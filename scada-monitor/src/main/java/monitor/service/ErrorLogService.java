package monitor.service;

import monitor.entity.ErrorLog;
import monitor.repository.ErrorLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ErrorLogService {

    private final ErrorLogRepository errorLogRepository;

    /**
     * Логирование ошибки
     *
     * @param errorType    Тип ошибки (VALIDATION_ERROR, KAFKA_ERROR, etc.)
     * @param sensorId     ID датчика (может быть null)
     * @param controllerId ID контроллера (может быть null)
     * @param userId       ID пользователя (может быть null)
     * @param message      Сообщение об ошибке
     * @param details      Детали ошибки (может быть null)
     */
    @Transactional
    public void logError(ErrorLog.ErrorType errorType,
                         String sensorId,
                         String controllerId,
                         String userId,
                         String message,
                         String details) {

        ErrorLog errorLog = ErrorLog.builder()
                .errorType(errorType)
                .sensorId(sensorId)
                .controllerId(controllerId)
                .userId(userId)
                .message(message)
                .details(details)
                .build();

        errorLogRepository.save(errorLog);
        log.warn("❌ Ошибка записана: {} - {}", errorType, message);
    }

    /**
     * Логирование ошибки (упрощённая версия, только для датчика)
     *
     * @param errorType Тип ошибки
     * @param sensorId  ID датчика
     * @param message   Сообщение об ошибке
     */
    @Transactional
    public void logError(ErrorLog.ErrorType errorType, String sensorId, String message) {
        logError(errorType, sensorId, null, null, message, null);
    }

    /**
     * Логирование ошибки с деталями (для датчика)
     *
     * @param errorType Тип ошибки
     * @param sensorId  ID датчика
     * @param message   Сообщение об ошибке
     * @param details   Детали ошибки (JSON или текст)
     */
    @Transactional
    public void logError(ErrorLog.ErrorType errorType, String sensorId, String message, String details) {
        logError(errorType, sensorId, null, null, message, details);
    }

    /**
     * Логирование ошибки для контроллера
     *
     * @param errorType    Тип ошибки
     * @param controllerId ID контроллера
     * @param message      Сообщение об ошибке
     */
    @Transactional
    public void logControllerError(ErrorLog.ErrorType errorType, String controllerId, String message) {
        logError(errorType, null, controllerId, null, message, null);
    }

    /**
     * Логирование ошибки с исключением
     *
     * @param errorType Тип ошибки
     * @param sensorId  ID датчика (может быть null)
     * @param e         Исключение
     */
    @Transactional
    public void logException(ErrorLog.ErrorType errorType, String sensorId, Exception e) {
        String message = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
        String stackTrace = getStackTrace(e);
        logError(errorType, sensorId, null, null, message, stackTrace);
    }

    /**
     * Логирование ошибки с исключением для контроллера
     *
     * @param errorType    Тип ошибки
     * @param controllerId ID контроллера
     * @param e            Исключение
     */
    @Transactional
    public void logControllerException(ErrorLog.ErrorType errorType, String controllerId, Exception e) {
        String message = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
        String stackTrace = getStackTrace(e);
        logError(errorType, null, controllerId, null, message, stackTrace);
    }

    /**
     * Логирование ошибки с исключением для пользователя
     *
     * @param errorType Тип ошибки
     * @param userId    ID пользователя
     * @param e         Исключение
     */
    @Transactional
    public void logUserException(ErrorLog.ErrorType errorType, String userId, Exception e) {
        String message = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
        String stackTrace = getStackTrace(e);
        logError(errorType, null, null, userId, message, stackTrace);
    }

    /**
     * Логирование ошибки валидации данных
     *
     * @param sensorId ID датчика
     * @param value    Значение, которое не прошло валидацию
     * @param min      Минимальное допустимое значение
     * @param max      Максимальное допустимое значение
     */
    @Transactional
    public void logOutOfRangeError(String sensorId, double value, double min, double max) {
        String message = String.format("Значение %.2f вне допустимого диапазона [%.2f, %.2f]", value, min, max);
        logError(ErrorLog.ErrorType.OUT_OF_RANGE, sensorId, message);
    }

    /**
     * Получить stack trace в виде строки (ограниченный размер)
     *
     * @param e Исключение
     * @return Строка с первыми 2000 символами stack trace
     */
    private String getStackTrace(Exception e) {
        StringBuilder sb = new StringBuilder();
        sb.append(e.toString()).append("\n");
        for (StackTraceElement element : e.getStackTrace()) {
            sb.append("  at ").append(element.toString()).append("\n");
            if (sb.length() > 2000) {
                sb.append("  ... (truncated)");
                break;
            }
        }
        return sb.toString();
    }

    /**
     * Получить количество ошибок по типу
     *
     * @param errorType Тип ошибки
     * @return Количество ошибок данного типа
     */
    public long countByType(ErrorLog.ErrorType errorType) {
        return errorLogRepository.findByErrorType(errorType, null).getTotalElements();
    }
}