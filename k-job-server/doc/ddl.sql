create table k_job.app_info
(
    id             bigint       not null
        primary key,
    current_server varchar(255) null,
    password       varchar(255) null,
    sub_app_name   varchar(255) null,
    app_name       varchar(255) null
);

create table k_job.distributed_lock
(
    id              bigint auto_increment
        primary key,
    lock_name       varchar(255) not null,
    lock_owner      varchar(255) not null,
    expiration_time timestamp    not null
);



create table k_job.instance_info
(
    id                    bigint auto_increment
        primary key,
    actual_trigger_time   bigint       null,
    expected_trigger_time bigint       null,
    finished_time         bigint       null,
    gmt_create            datetime(6)  null,
    gmt_modified          datetime(6)  null,
    instance_id           bigint       null,
    instance_params       longtext     null,
    job_id                bigint       null,
    job_params            longtext     null,
    last_report_time      bigint       null,
    result                longtext     null,
    running_times         bigint       null,
    status                int          null,
    task_tracker_address  varchar(255) null,
    type                  int          null,
    wf_instance_id        bigint       null,
    app_name              varchar(255) null
);

create index idx01_instance_info
    on k_job.instance_info (job_id, status);

create index idx02_instance_info
    on k_job.instance_info (status);

create index idx03_instance_info
    on k_job.instance_info (instance_id, status);





create table k_job.job_info
(
    id                   bigint auto_increment
        primary key,
    dispatch_strategy    int          null,
    gmt_create           datetime(6)  null,
    gmt_modified         datetime(6)  null,
    job_description      varchar(255) null,
    job_name             varchar(255) null,
    job_params           longtext     null,
    lifecycle            varchar(255) null,
    max_instance_num     int          null,
    next_trigger_time    bigint       null,
    processor_info       varchar(255) null,
    status               int          null,
    time_expression      varchar(255) null,
    time_expression_type int          null,
    job_id               mediumtext   not null,
    app_name             varchar(255) null
);

create index idx01_job_info
    on k_job.job_info (status, time_expression_type, next_trigger_time);

