package ru.telematica.casco2go.service.JourneyService;

import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;

import ru.telematica.casco2go.model.eventbus.ErrorEvent;
import ru.telematica.casco2go.repository.ConfigRepository;
import ru.telematica.casco2go.service.http.RetrofitProvider;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import ru.telematica.casco2go.service.ScoringService;

/**
 * Класс, отвечающий за отправку данных о поездке
 */

public final class JourneyDataSender implements IJourneyDataSource {

    private static final String TAG = JourneyDataSender.class.getSimpleName();

    private static final String TCP_HOST = RetrofitProvider.scroingTcpHost;
    private static final int TCP_PORT = RetrofitProvider.scroingTcpPort;

    private static final long TRANSFER_PERIOD = 30000;
    private static final int MSG_TRANSFER = 1;

    private static volatile JourneyDataSender instance = null;

    private final List<JourneyDataChunk> mDataBuffer = new ArrayList<>();

    private final IDataSender tcpService;
    private long mLastBackupTimestamp;

    private final Handler taskHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_TRANSFER:
                    checkForUnsentData();
                    break;
                default:
                    throw new IllegalStateException("Wrong message type");
            }
        }
    };

    private long mLastTransferTimestamp;

    private JourneyDataSender() {
        tcpService = new JourneyDataSenderService(this);
    }

    public static JourneyDataSender getInstance() {
        JourneyDataSender localInstance = instance;
        if (localInstance == null) {
            synchronized (JourneyDataSender.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new JourneyDataSender();
                }
            }
        }
        return instance;
    }

    public void checkForUnsentData() {
        // Чистим возможные отложенные вызовы этого метода
        taskHandler.removeMessages(MSG_TRANSFER);

        if (hasJourneyData()) {
            if (!tcpService.isSending()) {
                tcpService.start();
            }
        }
    }

    private void scheduleTransfer() {
        if (!tcpService.isSending() && !taskHandler.hasMessages(MSG_TRANSFER)) {
            long timeSinceLastTransfer = System.currentTimeMillis() - mLastTransferTimestamp;
            long delay = Math.max(0, TRANSFER_PERIOD - timeSinceLastTransfer);
            taskHandler.sendEmptyMessageDelayed(MSG_TRANSFER, delay);
        }
    }

    public void stopDataTransfer() {
        taskHandler.removeCallbacksAndMessages(null);
        if (tcpService.isSending()) {
            tcpService.stop();
        }
        mDataBuffer.clear();
    }

    public void sendJourneyData(Location location, float... acceleration) {
        Log.d(TAG, String.format("sendJourneyData: Latitude = %.8f, Longitude = %.8f", location.getLatitude(), location.getLongitude()));
        addChunk(JourneyDataChunk.journeyData(location, acceleration));
    }

    private void addChunk(JourneyDataChunk chunk) {
        synchronized (this) {
            if (mDataBuffer.isEmpty()) {
                // Добавляем первую порцию данных - с этого момента начинаем отсчёт отложенных процедур
                mLastBackupTimestamp = System.currentTimeMillis();
                mLastTransferTimestamp = System.currentTimeMillis();
            }
            mDataBuffer.add(chunk);
            scheduleTransfer();
        }
    }

    @Override
    public boolean hasJourneyData() {
        return !mDataBuffer.isEmpty();
    }

    @Override
    @Nullable
    public JourneyDataChunk getNextChunk() {
        synchronized (this) {
            if (hasJourneyData()) {
                return mDataBuffer.get(0);
            }
            return null;
        }
    }

    @Override
    public void onChunkSent(JourneyDataChunk chunk) {
        Log.d(TAG, "gps chunk sended");
        synchronized (this) {
            mDataBuffer.remove(chunk);
        }
    }

    private interface IDataSender {
        boolean isSending();

        void start();

        void stop();

        void onError(Throwable t);
    }

    private static class JourneyDataSenderService extends TcpClientAsync implements IDataSender {

        private final static String TAG = JourneyDataSenderService.class.getSimpleName();
        private final String host = TCP_HOST;
        private final int port = TCP_PORT;

        private final IJourneyDataSource mJourneyDataSource;
        private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());
        private boolean mSending;
        private final Runnable mRetryTask = new Runnable() {
            @Override
            public void run() {
                if (mSending) {
                    connect();
                }
            }
        };

        private JourneyDataSenderService(IJourneyDataSource journeyDataSource) {
            mJourneyDataSource = journeyDataSource;
        }

        /**
         * @return true, если идёт передача данных
         */
        @Override
        public boolean isSending() {
            return mSending;
        }

        /**
         * Запуск передачи данных (если авторизованы и есть что передавать)
         */
        @Override
        public void start() {
            stop();
            if (ConfigRepository.INSTANCE.getAuthData().hasAuth() && mJourneyDataSource.hasJourneyData()) {
                mSending = true;
                connect();
            }
        }

        /**
         * Прерываем передачу
         */
        @Override
        public void stop() {
            if (mSending) {
                mSending = false;
                stopClient();
            }
            mainThreadHandler.removeCallbacksAndMessages(null);
        }

        @Override
        public void onError(Throwable t) {
            // EventBus.getDefault().post(new ErrorEvent("", t));
        }

        /**
         * Повторная попытка пересылки через заданное время.
         * Вызывается, если очередная попытка пересылки не закончилась успешной передачей всех имеющихся данных
         */
        private void scheduleRetry() {
            mainThreadHandler.removeCallbacks(mRetryTask);
            mainThreadHandler.postDelayed(mRetryTask, 5000);
        }

        /**
         * Установка соединения и пересылка данных
         */
        private void connect() {
            run(host, port, new TcpClientAsync.Interface() {
                private boolean mTransferAuthorized;

                private boolean sendChunk(JourneyDataChunk chunk) {
                    if (chunk == null) {
                        return false;
                    }

                    final ByteArrayOutputStream chunkByteStream = chunk.toByteStream();
                    boolean success = chunkByteStream != null && sendMessage(chunkByteStream.toByteArray());

                    Log.d(TAG, "sendChunk(): " + chunk.toText());
                    return success;
                }

                @Override
                public void onConnect() {
                    mTransferAuthorized = sendChunk(JourneyDataChunk.id());
                    if (mTransferAuthorized) {
                        mTransferAuthorized = sendChunk(JourneyDataChunk.journeyStart());
                    }
                }

                @Override
                public void onRequest() {
                    if (mTransferAuthorized) {
                        while (mSending && mJourneyDataSource.hasJourneyData()) {
                            final JourneyDataChunk dataChunk = mJourneyDataSource.getNextChunk();
                            if (sendChunk(dataChunk)) {
                                mJourneyDataSource.onChunkSent(dataChunk);
                            } else {
                                // Что-то пошло не так - выходим из цикла
                                break;
                            }
                        }
                    }

                    // Рвём соединение и проверяем, всё ли удалось отослать за эту сессию
                    stopClient();
                    if (!mJourneyDataSource.hasJourneyData()) {
                        // Отослали всё, что было
                        mSending = false;
                    } else if (mSending) {

                        // Отослали не всё, и передачу не прервали извне - пробуем ещё раз чуть позже
                        scheduleRetry();
                    }
                }

                @Override
                public void onException(Exception e) {
                    if (mSending) {
                        Log.e(TAG, "sendData() failed", e);
                        // Что-то пошло не так, но передачу не прервали извне - пробуем ещё раз чуть позже
                        scheduleRetry();

                        onError(e);
                    }
                }

                @Override
                public void onResponse(char[] response) {
                    //Do nothing
                }
            });
        }
    }
}
