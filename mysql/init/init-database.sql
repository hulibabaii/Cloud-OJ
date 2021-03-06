create database cloud_oj character set utf8mb4;
use cloud_oj;
set character set utf8;
create table contest
(
    contest_id   int auto_increment
        primary key,
    contest_name varchar(64)   null,
    start_at     timestamp     not null comment '开始时间',
    end_at       timestamp     not null comment '结束时间',
    languages    int default 0 not null comment '支持的语言范围'
);

create table problem
(
    problem_id    int auto_increment
        primary key,
    title         varchar(64)                          not null,
    description   text                                 not null comment '题目描述',
    input         text                                 not null comment '输入说明',
    output        text                                 not null comment '输出说明',
    sample_input  text                                 null comment '示例输入',
    sample_output text                                 null comment '示例输出',
    timeout       bigint     default 1000              null comment '时间限制',
    score         int        default 0                 not null comment '分数',
    enable        tinyint(1) default 0                 null comment '是否可用',
    category      varchar(32)                          null comment '分类',
    create_at     datetime   default CURRENT_TIMESTAMP not null comment '创建时间'
);

create table `contest-problem`
(
    contest_id int null,
    problem_id int null,
    constraint `contest-problem_contest_contest_id_fk`
        foreign key (contest_id) references contest (contest_id)
            on update cascade on delete cascade,
    constraint `contest-problem_problem_problem_id_fk`
        foreign key (problem_id) references problem (problem_id)
            on update cascade on delete cascade
);

create index problem_title_index
    on problem (title);

create table role
(
    role_id   int         not null
        primary key,
    role_name varchar(32) not null
);

create table user
(
    user_id   varchar(32)                         not null
        primary key,
    name      varchar(16)                         not null comment '用户名',
    password  varchar(100)                        not null,
    secret    char(8)                             not null comment '秘钥',
    email     varchar(32)                         null,
    section   varchar(16)                         null comment '班级',
    role_id   int       default 0                 not null,
    create_at timestamp default CURRENT_TIMESTAMP null,
    constraint user_role_role_id_fk
        foreign key (role_id) references role (role_id)
            on update cascade
);

create table solution
(
    solution_id char(36)
        primary key,
    problem_id  int                                null,
    contest_id  int                                null,
    language    int                                not null,
    state       int      default 2                 not null comment '状态(2->已提交,1->在判题队列,0->已判题)',
    result      int                                null comment '结果(4->编译错误,3->答案错误,2->部分通过,1->时间超限,0->完全正确)',
    pass_rate   double   default 0                 not null comment '通过率',
    user_id     varchar(32)                        null,
    submit_time datetime default CURRENT_TIMESTAMP not null comment '提交时间',
    constraint solution_contest_contest_id_fk
        foreign key (contest_id) references contest (contest_id)
            on update cascade,
    constraint solution_problem_problem_id_fk
        foreign key (problem_id) references problem (problem_id)
            on update cascade on delete cascade,
    constraint solution_user_user_id_fk
        foreign key (user_id) references user (user_id)
            on update cascade
);

create table compile
(
    id          int auto_increment
        primary key,
    solution_id char(36) not null,
    state       int      not null comment '编译状态(0->编译成功,-1->编译出错)',
    info        text     null comment '编译信息',
    constraint compile_solution_solution_id_fk
        foreign key (solution_id) references solution (solution_id)
            on update cascade on delete cascade
);

create table runtime
(
    id          int auto_increment
        primary key,
    solution_id char(36) not null,
    total       int      null comment '总测试点数量',
    passed      int      null comment '通过的测试点数量',
    time        bigint   null comment '耗时(ms)',
    memory      bigint   null comment '内存占用(KB)',
    info        text     null,
    constraint runtime_solution_solution_id_fk
        foreign key (solution_id) references solution (solution_id)
            on update cascade on delete cascade
);

create table source_code
(
    code_id     int auto_increment
        primary key,
    solution_id char(36) not null,
    code        text     not null,
    constraint source_code_solution_solution_id_fk
        foreign key (solution_id) references solution (solution_id)
            on update cascade on delete cascade
);

create index user_name_index
    on user (name);

create index user_section_index
    on user (section);

DELIMITER //
create definer = root@`%` trigger tr_user_update
    before update
    on user
    for each row
begin
    if OLD.role_id <> NEW.role_id or OLD.password <> NEW.password
    then
        set NEW.secret = LEFT(UUID(), 8);
    end if;
end //
DELIMITER ;

create definer = root@`%` view contest_problem as
select `c`.`contest_id`    AS `contest_id`,
       `c`.`contest_name`  AS `contest_name`,
       `c`.`start_at`      AS `start_at`,
       `c`.`end_at`        AS `end_at`,
       `p`.`problem_id`    AS `problem_id`,
       `p`.`title`         AS `title`,
       `p`.`description`   AS `description`,
       `p`.`input`         AS `input`,
       `p`.`output`        AS `output`,
       `p`.`sample_input`  AS `sample_input`,
       `p`.`sample_output` AS `sample_output`,
       `p`.`timeout`       AS `timeout`,
       `p`.`score`         AS `score`,
       `p`.`enable`        AS `enable`,
       `p`.`category`      AS `category`,
       `p`.`create_at`     AS `create_at`
from ((`cloud_oj`.`contest-problem` `cp` join `cloud_oj`.`problem` `p` on ((`cp`.`problem_id` = `p`.`problem_id`)))
         join `cloud_oj`.`contest` `c` on ((`cp`.`contest_id` = `c`.`contest_id`)));

create definer = root@`%` view contest_ranking_list as
select `ranking`.`user_id`      AS `user_id`,
       `ranking`.`name`         AS `name`,
       `ranking`.`committed`    AS `committed`,
       `passed`.`passed`        AS `passed`,
       `ranking`.`total_score`  AS `total_score`,
       `ranking`.`contest_id`   AS `contest_id`,
       `ranking`.`contest_name` AS `contest_name`
from ((select `problem_score`.`user_id`        AS `user_id`,
              `problem_score`.`name`           AS `name`,
              sum(`problem_score`.`committed`) AS `committed`,
              sum(`problem_score`.`score`)     AS `total_score`,
              `problem_score`.`contest_id`     AS `contest_id`,
              `problem_score`.`contest_name`   AS `contest_name`
       from (select `s`.`user_id`                        AS `user_id`,
                    `u`.`name`                           AS `name`,
                    count(`p`.`problem_id`)              AS `committed`,
                    max((`s`.`pass_rate` * `p`.`score`)) AS `score`,
                    `s`.`contest_id`                     AS `contest_id`,
                    `c`.`contest_name`                   AS `contest_name`
             from (((`cloud_oj`.`solution` `s` join `cloud_oj`.`user` `u` on ((`s`.`user_id` = `u`.`user_id`) and (`u`.`role_id` = 0)))
                 join `cloud_oj`.`problem` `p` on ((`s`.`problem_id` = `p`.`problem_id`)))
                      join `cloud_oj`.`contest` `c` on ((`s`.`contest_id` = `c`.`contest_id`)))
             group by `s`.`user_id`, `u`.`name`, `p`.`problem_id`, `s`.`contest_id`, `c`.`contest_name`) `problem_score`
       group by `problem_score`.`user_id`, `problem_score`.`contest_id`) `ranking`
         join (select `t`.`user_id` AS `user_id`, count((case when (`t`.`result` = 0) then 1 end)) AS `passed`
               from (select `s`.`user_id` AS `user_id`, `s`.`result` AS `result`
                     from ((`cloud_oj`.`solution` `s` join `cloud_oj`.`user` `u` on ((`s`.`user_id` = `u`.`user_id`)))
                              join `cloud_oj`.`contest` `c` on ((`s`.`contest_id` = `c`.`contest_id`)))
                     group by `s`.`problem_id`, `s`.`user_id`, `c`.`contest_id`, `s`.`result`) `t`
               group by `t`.`user_id`) `passed` on ((`ranking`.`user_id` = `passed`.`user_id`)));

create definer = root@`%` view judge_result as
select `s`.`solution_id`                         AS `solution_id`,
       `s`.`problem_id`                          AS `problem_id`,
       `s`.`contest_id`                          AS `contest_id`,
       `p`.`title`                               AS `title`,
       `s`.`language`                            AS `language`,
       `s`.`state`                               AS `state`,
       `s`.`result`                              AS `result`,
       round(`s`.`pass_rate`, 2)                 AS `pass_rate`,
       `s`.`user_id`                             AS `user_id`,
       `s`.`submit_time`                         AS `submit_time`,
       round((`s`.`pass_rate` * `p`.`score`), 1) AS `score`,
       `sc`.`code`                               AS `code`,
       `r`.`time`                                AS `time`,
       `r`.memory                                AS `memory`
from ((((`cloud_oj`.`solution` `s` join `cloud_oj`.`problem` `p` on ((`s`.`problem_id` = `p`.`problem_id`))) join `cloud_oj`.`compile` `c` on ((`s`.`solution_id` = `c`.`solution_id`))) left join `cloud_oj`.`runtime` `r` on ((`s`.`solution_id` = `r`.`solution_id`)))
         join `cloud_oj`.`source_code` `sc` on ((`s`.`solution_id` = `sc`.`solution_id`)))
order by `s`.`submit_time`;

create definer = root@`%` view ranking_list as
select `ranking`.`user_id`     AS `user_id`,
       `ranking`.`name`        AS `name`,
       `ranking`.`committed`   AS `committed`,
       `passed`.`passed`       AS `passed`,
       `ranking`.`total_score` AS `total_score`
from ((select `problem_score`.`user_id`        AS `user_id`,
              `problem_score`.`name`           AS `name`,
              sum(`problem_score`.`committed`) AS `committed`,
              sum(`problem_score`.`score`)     AS `total_score`
       from (select `s`.`user_id`                        AS `user_id`,
                    `u`.`name`                           AS `name`,
                    count(`p`.`problem_id`)              AS `committed`,
                    max((`s`.`pass_rate` * `p`.`score`)) AS `score`
             from ((`cloud_oj`.`solution` `s` join `cloud_oj`.`user` `u` on ((`s`.`user_id` = `u`.`user_id`) and (`u`.`role_id` = 0)))
                      join `cloud_oj`.`problem` `p` on ((`s`.`problem_id` = `p`.`problem_id`)))
             where contest_id is null
             group by `s`.`user_id`, `u`.`name`, `p`.`problem_id`) `problem_score`
       group by `problem_score`.`user_id`) `ranking`
         join (select `t`.`user_id` AS `user_id`, count((case when (`t`.`result` = 0) then 1 end)) AS `passed`
               from (select `s`.`user_id` AS `user_id`, `s`.`result` AS `result`
                     from (`cloud_oj`.`solution` `s`
                              join `cloud_oj`.`user` `u` on ((`s`.`user_id` = `u`.`user_id`)))
                     group by `s`.`problem_id`, `s`.`user_id`, `s`.`result`) `t`
               group by `t`.`user_id`) `passed` on ((`ranking`.`user_id` = `passed`.`user_id`)));

-- 初始化角色/权限表
INSERT INTO cloud_oj.role (role_id, role_name)
VALUES (0, 'ROLE_USER');
INSERT INTO cloud_oj.role (role_id, role_name)
VALUES (1, 'ROLE_USER_ADMIN');
INSERT INTO cloud_oj.role (role_id, role_name)
VALUES (2, 'ROLE_PROBLEM_ADMIN');
INSERT INTO cloud_oj.role (role_id, role_name)
VALUES (3, 'ROLE_ROOT');

-- 初始 ROOT 用户
set character set utf8;
INSERT INTO cloud_oj.user (user_id, name, password, secret, role_id)
VALUES ('root', '初始管理员', '$2a$10$79exZxOfiSAtHcyCXSfjMeH5GYgMwUhexc.3ZXqbuxLaHVhp05LTi', LEFT(UUID(), 8), 3);

ALTER TABLE problem
    AUTO_INCREMENT = 1000