alter table event
    add column public_link varchar(255);

create table sport_match
(
    id          bigint auto_increment primary key,
    event_id    bigint       not null references event (id),
    name        varchar(32)  not null comment '名称',
    create_time datetime     null comment '创建时间',
    enabled     bit          null comment '是否启用'
);