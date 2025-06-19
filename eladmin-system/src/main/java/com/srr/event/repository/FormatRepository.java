package com.srr.event.repository;

import com.srr.event.domain.Format;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface FormatRepository extends JpaRepository<Format, Long>, JpaSpecificationExecutor<Format> {
}
