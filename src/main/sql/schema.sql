create table Stops
(
	tripId varchar(64) null,
	arrivalTime char(5) null,
	nextDay tinyint(1) null,
	stopId varchar(32) null,
	stopSequence int null,
	constraint Stops_routeId_stopSequence_uindex
		unique (tripId, stopSequence)
)
engine=InnoDB
;

create table TripResults
(
	tripDate date null,
	tripTS timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP,
	routeId varchar(32) null,
	nextStop varchar(24) null,
	stopSequence int null,
	scheduledTiem time null,
	predictedTime time null,
	delay mediumtext null,
	timeTilNextStop mediumtext null
)
engine=InnoDB
;

create index TripResults_tripDate_routeId_index
	on TripResults (tripDate, routeId)
;

create table Trips
(
	routeId varchar(32) null,
	tripId varchar(64) not null,
	headSign varchar(24) null,
	direction char null
)
engine=InnoDB
;

create index Trips_routeId_index
	on Trips (routeId)
;

create index Trips_tripId_routeId_index
	on Trips (tripId, routeId)
;

