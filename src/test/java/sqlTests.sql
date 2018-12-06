SELECT * FROM ArcheryArcherRounds WHERE shootDate BETWEEN date('04 Dec 17 14:27') AND date('04 Dec 19 14:27') ORDER BY shootDate;
SELECT * FROM ArcheryArcherRounds WHERE shootDate < datetime('15:12 04/12/17');
SELECT datetime('2017-12-04 15:12') FROM ArcheryArcherRounds WHERE shootDate < date('now', '-1 years');
