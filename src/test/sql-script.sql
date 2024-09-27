# 0.1304
SELECT count(DISTINCT(concat(i.coll_id, i.date, i.status)))/count(*) AS Selectivity
FROM invigilation i;

# 0.4340
SELECT count(DISTINCT(concat(i.department ->> '$.depId', i.date, i.status)))/count(*) AS Selectivity
FROM invigilation i;

# 0.6320
SELECT count(DISTINCT(concat(t.user_id, t.dayweek, t.startweek, t.endweek)))/count(*) AS Selectivity
FROM timetable t;