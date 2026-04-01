package monitor.repository;

import monitor.entity.ErrorLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface ErrorLogRepository extends JpaRepository<ErrorLog, Long> {

    Page<ErrorLog> findByErrorType(ErrorLog.ErrorType errorType, Pageable pageable);

    Page<ErrorLog> findBySensorId(String sensorId, Pageable pageable);

    Page<ErrorLog> findByCreatedAtBetween(LocalDateTime from, LocalDateTime to, Pageable pageable);
}