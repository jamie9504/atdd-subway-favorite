create table if not exists STATION
(
    id         bigint auto_increment not null,
    name       varchar(255)          not null unique,
    created_at datetime,
    primary key (id)
);

create table if not exists LINE
(
    id            bigint auto_increment not null,
    name          varchar(255)          not null,
    start_time    time                  not null,
    end_time      time                  not null,
    interval_time int                   not null,
    created_at    datetime,
    updated_at    datetime,
    primary key (id)
);

create table if not exists LINE_STATION
(
    id             bigint auto_increment not null,
    line           bigint                not null,
    station_id     bigint                not null,
    pre_station_id bigint,
    distance       int,
    duration       int,
    created_at     datetime,
    updated_at     datetime,
    foreign key (station_id) references STATION (id) on delete cascade,
    foreign key (pre_station_id) references STATION (id) on delete cascade
);

create table if not exists MEMBER
(
    id       bigint auto_increment not null,
    email    varchar(255)          not null unique,
    name     varchar(255)          not null,
    password varchar(255)          not null,
    primary key (id)
);

create table if not exists FAVORITE
(
    id                bigint auto_increment not null,
    member            bigint                not null,
    source_station_id bigint                not null,
    target_station_id bigint                not null,
    foreign key (source_station_id) references STATION (id) on delete cascade,
    foreign key (target_station_id) references STATION (id) on delete cascade,
    unique key uk_favorite (member, source_station_id, target_station_id)
);