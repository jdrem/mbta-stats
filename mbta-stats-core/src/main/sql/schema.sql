create table Calendar
(
	calendarName varchar(16) not null,
	calendarType enum('Weekday', 'Saturday', 'Sunday') not null,
	startDate date not null,
	endDate date not null
)
engine=InnoDB
;

create table CalendarExceptions
(
	calendarName varchar(16) not null,
	exceptionDate date not null,
	exceptionType enum('Weekday', 'Saturday', 'Sunday') not null
)
engine=InnoDB
;

create table Routes
(
	routeId int auto_increment
		primary key,
	routeName varchar(32) not null,
	constraint Routes_routeId_uindex
		unique (routeId)
)
engine=InnoDB
;

create table Stops
(
	tripId int not null,
	arrivalTime time not null,
	nextDay tinyint(1) not null,
	stopName varchar(32) not null,
	stopSequence int not null,
	constraint StopsX_tripId_arrivalTime_uindex
		unique (tripId, arrivalTime)
)
engine=InnoDB
;

create table TripResults
(
	id int auto_increment
		primary key,
	tripDate date null,
	tripTS timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP,
	tripId int null,
	nextStop varchar(24) null,
	stopSequence int null,
	scheduledTime time null,
	predictedTime time null,
	delay mediumtext null,
	timeTilNextStop mediumtext null
)
engine=InnoDB
;

create index TripResults_tripDate_routeId_index
	on TripResults (tripDate, tripName)
;

create table Trips
(
	routeId int not null,
	tripId int not null
		primary key,
	ScheduleType enum('Weekday', 'Saturday', 'Sunday', 'ExtremeReuced') not null,
	headSign varchar(24) not null,
	direction char not null,
	constraint TripsX_tripId_uindex
		unique (tripId)
)
engine=InnoDB
;
