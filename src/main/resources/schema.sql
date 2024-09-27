create table if not exists `setting`
(
    id          char(19)     not null primary key,
    name        varchar(10)  not null,
    skey        varchar(10)  not null,
    svalue      varchar(200) not null,
    insert_time datetime     not null default current_timestamp,
    update_time datetime     not null default current_timestamp on update current_timestamp,

    unique (skey)
);

create table if not exists `user`
(
    id            char(19)         not null primary key,
    name          varchar(6)       not null,
    account       varchar(15)      not null,
    password      varchar(65)      not null,
    department    json             null comment '{collId, collegeName, depId, departmentName}',
    mobile        char(11)         null,
    role          char(5)          not null,
    ding_union_id varchar(50)      null,
    ding_user_id  varchar(40)      null,
    description   varchar(60)      null,
    invi_status   tinyint unsigned null     default 1,
    insert_time   datetime         not null default current_timestamp,
    update_time   datetime         not null default current_timestamp on update current_timestamp,

    unique (account),
    index ((cast(department ->> '$.depId' as char(19)) collate utf8mb4_bin))
);

create table if not exists `department`
(
    id          char(19)         not null primary key,
    name        varchar(20)      not null,
    college     json             null comment '{collId, collegeName}',
    root        tinyint unsigned null     default null,
    invi_status tinyint unsigned null     default 1,
    ding_depid  varchar(50)      null,
    comment     varchar(500)     null     default null,
    insert_time datetime         not null default current_timestamp,
    update_time datetime         not null default current_timestamp on update current_timestamp,

    index ((cast(college ->> '$.collId' as char(19)) collate utf8mb4_bin)),
    index (root)
);

create table if not exists `timetable`
(
    id           char(19)         not null primary key,
    coll_id      char(19)         not null,
    startweek    tinyint unsigned not null,
    endweek      tinyint unsigned not null,
    dayweek      tinyint unsigned not null,
    `period`     varchar(6)       not null,
    course       json             null comment '{courseName, location, clazz}',
    user_id      char(19)         not null,
    teacher_name varchar(6)       not null,
    insert_time  datetime         not null default current_timestamp,
    update_time  datetime         not null default current_timestamp on update current_timestamp,

    index (coll_id),
    index (user_id, dayweek, startweek, endweek)
);

create table if not exists `invigilation`
(
    id              char(19)         not null primary key,
    coll_id         char(19)         not null,
    department      json             null comment '{depId, departmentName}',
    importer        json             null comment '{userId, userName, updateTime}',
    dispatcher      json             null comment '{userId, userName, updateTime}',
    allocator       json             null comment '{userId, userName, updateTime}',
    executor        json             null comment '[{userId, userName}]',
    date            date             not null,
    time            json             not null comment '{starttime, endtime}',
    course          json             not null comment '{courseName, teacherName, location, clazz}',
    amount          tinyint unsigned not null,
    status          tinyint unsigned not null,
    calendar_id     varchar(50)      null comment 'dingtalk',
    create_union_id varchar(50)      null,
    notice_user_ids json             null comment '[userId]',
    remark          varchar(100)     null,
    insert_time     datetime         not null default current_timestamp,
    update_time     datetime         not null default current_timestamp on update current_timestamp,

    index (coll_id, date, status),
    index ((cast(department ->> '$.depId' as char(19)) collate utf8mb4_bin), date, status)
);

create table if not exists `invi_detail`
(
    id          char(19) not null primary key,
    invi_id     char(19) not null,
    user_id     char(19) not null,
    insert_time datetime not null default current_timestamp,
    update_time datetime not null default current_timestamp on update current_timestamp,

    index (invi_id),
    index (user_id)
);

/*
 排除监考规则
 */
create table if not exists `exclude_rule`
(
    id           char(19)         not null primary key,
    dep_id       char(19)         not null,
    user_id      char(19)         not null,
    teacher_name varchar(10)      null,
    startweek    tinyint unsigned not null,
    endweek      tinyint unsigned not null,
    dayweeks     json             not null comment '[1,2,3]',
    `periods`    json             not null comment '["12", "78"]',
    insert_time  datetime         not null default current_timestamp,
    update_time  datetime         not null default current_timestamp on update current_timestamp,

    index (dep_id)
)
