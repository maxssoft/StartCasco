package ru.telematica.casco2go.service.JourneyService;

/**
 * Интерфейс с источником данных о поездке, передаваемых через TCP
 */

interface IJourneyDataSource {

    /**
     * @return true, если есть данные для пересылки
     */
    boolean hasJourneyData();

    /**
     * Получение очередной порции данных для отправки
     *
     * @return Сегмент поездки, стоящий на очереди на отправку, либо null если очередь пуста
     */
    JourneyDataChunk getNextChunk();

    /**
     * Порция данных о поездке успешно отправлена
     */
    void onChunkSent(JourneyDataChunk chunk);
}
