package com.srr.event.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface FormatRepository extends JpaRepository<Format, Long>, JpaSpecificationExecutor<Format> {
}
