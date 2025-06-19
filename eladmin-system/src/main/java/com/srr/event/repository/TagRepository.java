package com.srr.event.repository;

import com.srr.event.domain.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface TagRepository extends JpaRepository<Tag, Long>, JpaSpecificationExecutor<Tag> {

    /**
     * Find tag by name.
     * @param name Tag name
     * @return Tag
     */
    Optional<Tag> findByName(String name);

    /**
     * Find tags by a list of names.
     * @param names List of tag names
     * @return Set of Tags
     */
    Set<Tag> findByNameIn(List<String> names);
}
