package ru.practicum.ewmservice.request;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RequestRepository extends JpaRepository<Request, Long> {

    @Query("select count(r) " +
            "from Request r " +
            "where r.event.id = ?1 " +
            "and r.status = ?2")
    Integer findCountRequestsByEventIdAndStatus(Long eventId, RequestStatus status);

    List<Request> findByEventId(Long eventId);

    List<Request> findByIdIn(List<Long> ids);

    List<Request> findByRequesterId(Long id);

    Boolean existsByEventIdAndRequesterId(Long eventId, Long requesterId);
}
