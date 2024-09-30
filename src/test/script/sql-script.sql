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

# Using index condition
explain
select t.id from timetable t
where t.coll_id='1259782025195839488';

# Using index condition
explain
select * from timetable t
where t.coll_id='1259782025195839488';

# Using index condition
explain
select * from timetable t
where t.user_id='1265883399983886340';

# Using index condition; 复合索引
explain
select * from timetable t
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