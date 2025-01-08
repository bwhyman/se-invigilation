# 0.1304
SELECT count(DISTINCT(concat(i.coll_id, i.date, i.status)))/count(*) AS Selectivity
FROM invigilation i;
# 计算可选择度与顺序无关；
# 0.1304
SELECT count(DISTINCT(concat(i.date, i.status)))/count(*) AS Selectivity
FROM invigilation i;

# 0.4340
SELECT count(DISTINCT(concat(i.department ->> '$.depId', i.date, i.status)))/count(*) AS Selectivity
FROM invigilation i;

# 0.6320
SELECT count(DISTINCT(concat(t.user_id, t.dayweek, t.startweek, t.endweek)))/count(*) AS Selectivity
FROM timetable t;

# Using where; Using index
explain
select t.id from timetable t
where t.coll_id='1259782025195839488';

# Using index condition
explain
select * from timetable t
where t.coll_id='1259782025195839488';

# Using index condition; Using where. dayweek不在collid索引
explain
select * from timetable t
where t.coll_id='1259782025195839488' and t.dayweek=1;

# Using index condition
explain
select * from timetable t
where t.user_id='1265883399983886340';

# Using where; Using index. 复合索引，查询字段在索引
explain
select t.dayweek from timetable t
where t.user_id='1265883399983886340';

# Using index condition; 复合索引
explain
select * from timetable t
where t.user_id='1265883399983886340' and t.dayweek=1;

# Using index condition; 复合索引
explain
select t.course from timetable t
where t.user_id='1265883399983886340' and t.dayweek=1;

# Using where; Using index; startweek复合索引
explain
select t.startweek from timetable t
where t.user_id='1265883399983886340' and t.dayweek=1;

# Using index condition; Using where
explain
select * from timetable t
where t.user_id='1265883399983886340' and t.dayweek=1 and t.period='12';

# Using index condition; Using where； period不在复合索引
explain
select * from timetable t
where t.user_id='1265883399983886340' and t.period='12';

explain
select * from timetable t
where t.dayweek=1;


flush status ;
show session status like 'handler%';

explain
select * from department d where d.college ->> '$.collId' = '1154814591036186624';

/* on无法命中索引 */
explain
select d.id, count(d.id) as count from department d join user u
on u.department ->> '$.depId'=d.id
join invi_detail ivd
on u.id=ivd.user_id
where d.college ->> '$.collId'='1154814591036186624' and u.invi_status=1
group by d.id;

explain
select u.department ->> '$.depId' as depid, count(u.department ->> '$.depId') as count from user u
join invi_detail ivd
on u.id=ivd.user_id
where u.department ->> '$.collId'='1154814591036186624' and u.invi_status=1
group by u.department ->> '$.depId';


explain
select u.department ->> '$.depId' as depid, count(u.department ->> '$.depId') as count from user u where u.department ->> '$.collId'='1154814591036186624' and u.invi_status=1
group by u.department ->> '$.depId';

explain
with quantity as (
    select u.department ->> '$.depId' as depid, count(u.department ->> '$.depId') as count from user u
    join invi_detail ivd
    on u.id=ivd.user_id
    where u.department ->> '$.collId'='1154814591036186624' and u.invi_status=1
    group by u.department ->> '$.depId'
),
count as (
    select u.department ->> '$.depId' as depid, count(u.department ->> '$.depId') as count from user u where u.department ->> '$.collId'='1154814591036186624' and u.invi_status=1
    group by u.department ->> '$.depId'
)
select q.depid, round(q.count/c.count, 1) avg from quantity q left join count c on q.depid=c.depid;
