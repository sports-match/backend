package com.srr.player.domain;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.srr.enumeration.Gender;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import me.zhengjie.modules.system.domain.User;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;

/**
 * @author Chanheng
 * @description
 * @date 2025-05-18
 **/
@Entity
@Getter
@Setter
@Table(name = "player")
public class Player implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @ApiModelProperty(value = "id", hidden = true)
    private Long id;

    @Column(name = "name", nullable = false)
    @NotBlank
    @ApiModelProperty(value = "Name")
    private String name;

    @Column(name = "description")
    @ApiModelProperty(value = "Description")
    private String description;

    @Column(name = "latitude")
    @ApiModelProperty(value = "Latitude")
    private Double latitude;

    @Column(name = "longitude")
    @ApiModelProperty(value = "Longitude")
    private Double longitude;

    @Column(name = "profile_image")
    @ApiModelProperty(value = "Image")
    private String profileImage;

    @Column(name = "create_time")
    @CreationTimestamp
    @ApiModelProperty(value = "Creation time", hidden = true)
    private Timestamp createTime;

    @Column(name = "update_time")
    @UpdateTimestamp
    @ApiModelProperty(value = "Update time", hidden = true)
    private Timestamp updateTime;

    @JoinColumn(name = "user_id")
    @OneToOne(fetch = FetchType.LAZY)
    private User user;

    @Column(name = "gender")
    @Enumerated(EnumType.STRING)
    @ApiModelProperty(value = "Gender")
    private Gender gender;

    @Column(name = "date_of_birth")
    @ApiModelProperty(value = "Date of Birth")
    private LocalDate dateOfBirth;

    @OneToMany(mappedBy = "player")
    private List<PlayerSportRating> playerSportRating;

    public void copy(Player source) {
        BeanUtil.copyProperties(source, this, CopyOptions.create().setIgnoreNullValue(true));
    }
}
