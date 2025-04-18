package com.calendarugr.academic_subscription_service.services;

import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.WeekDay;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.*;
import net.fortuna.ical4j.model.property.immutable.ImmutableCalScale;
import net.fortuna.ical4j.model.property.immutable.ImmutableVersion;
import net.fortuna.ical4j.transform.recurrence.Frequency;

import java.io.ByteArrayOutputStream;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import com.calendarugr.academic_subscription_service.dtos.ClassDTO;
import com.calendarugr.academic_subscription_service.dtos.ExtraClassDTO;

public class IcsGenerator {

    private WeekDay mapDayOfWeek(String day) {
        return switch (day.toLowerCase()) {
            case "lunes" -> WeekDay.MO;
            case "martes" -> WeekDay.TU;
            case "miércoles" -> WeekDay.WE;
            case "jueves" -> WeekDay.TH;
            case "viernes" -> WeekDay.FR;
            case "sábado" -> WeekDay.SA;
            case "domingo" -> WeekDay.SU;
            default -> throw new IllegalArgumentException("Día no válido: " + day);
        };
    }

    public byte[] generateICalendar(List<ClassDTO> classList) throws Exception {

        Calendar calendar = new Calendar();

        calendar.add(new ProdId("-//CalendarUGR//iCal4j 1.0//ES"));
        calendar.add(ImmutableVersion.VERSION_2_0);
        calendar.add(ImmutableCalScale.GREGORIAN);

        for (ClassDTO classDTO : classList) {
            VEvent event = createRecurringEvent(classDTO);
            calendar.add(event);
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CalendarOutputter outputter = new CalendarOutputter();
        outputter.output(calendar, baos);

        return baos.toByteArray();
    }

    public byte[] generateCompleteICalendar(HashMap<String, List<?>> classes) throws Exception {
        // We assume that is a "facultyEvents" key, a "classes" key and a "groupEvents"
        // key

        Calendar calendar = new Calendar();

        calendar.add(new ProdId("-//CalendarUGR//iCal4j 1.0//ES"));
        calendar.add(ImmutableVersion.VERSION_2_0);
        calendar.add(ImmutableCalScale.GREGORIAN);

        // Add faculty events
        List<ExtraClassDTO> facultyEvents = (List<ExtraClassDTO>) classes.get("facultyEvents");
        if (facultyEvents != null) {
            for (ExtraClassDTO extraClassDTO : facultyEvents) {
                VEvent event = createFacultyEvent(extraClassDTO);
                if (event != null) {
                    calendar.add(event);
                }
            }
        }

        // Add group events
        List<ExtraClassDTO> groupEvents = (List<ExtraClassDTO>) classes.get("groupEvents");

        if (groupEvents != null) {
            for (ExtraClassDTO extraClassDTO : groupEvents) {
                VEvent event = createGroupEvent(extraClassDTO);
                if (event != null) {
                    calendar.add(event);
                }
            }
        }

        // Add classes
        List<ClassDTO> classesList = (List<ClassDTO>) classes.get("classes");
        if (classesList != null) {
            for (ClassDTO classDTO : classesList) {
                VEvent event = createRecurringEvent(classDTO);
                calendar.add(event);
            }
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CalendarOutputter outputter = new CalendarOutputter();
        outputter.output(calendar, baos);

        return baos.toByteArray();
    }

    private VEvent createFacultyEvent(ExtraClassDTO extraClassDTO) {
        // Convertir LocalDate y LocalTime a ZonedDateTime
        ZoneId zoneId = ZoneId.of("Europe/Madrid"); // Zona horaria específica
        LocalDateTime startDateTime = LocalDateTime.of(extraClassDTO.getDate(), extraClassDTO.getInitHour());
        LocalDateTime endDateTime = LocalDateTime.of(extraClassDTO.getDate(), extraClassDTO.getFinishHour());

        ZonedDateTime startZoned = startDateTime.atZone(zoneId);
        ZonedDateTime endZoned = endDateTime.atZone(zoneId);

        VEvent event = new VEvent(startZoned, endZoned, extraClassDTO.getTitle());

        String uniqueId = java.util.UUID.randomUUID().toString() + "@calendarugr";
        event.add(new Uid(uniqueId));

        event.add(new Location(extraClassDTO.getFacultyName()));
        event.add(new Categories("Faculty Event"));

        return event;
    }

    private VEvent createGroupEvent(ExtraClassDTO extraClassDTO) {
        // Convertir LocalDate y LocalTime a ZonedDateTime
        ZoneId zoneId = ZoneId.of("Europe/Madrid"); // Zona horaria específica
        LocalDateTime startDateTime = LocalDateTime.of(extraClassDTO.getDate(), extraClassDTO.getInitHour());
        LocalDateTime endDateTime = LocalDateTime.of(extraClassDTO.getDate(), extraClassDTO.getFinishHour());

        ZonedDateTime startZoned = startDateTime.atZone(zoneId);
        ZonedDateTime endZoned = endDateTime.atZone(zoneId);

        VEvent event = new VEvent(startZoned, endZoned, extraClassDTO.getTitle());

        String uniqueId = java.util.UUID.randomUUID().toString() + "@calendarugr";
        event.add(new Uid(uniqueId));

        event.add(new Location(extraClassDTO.getGroupName()));
        event.add(new Categories("Group Event"));

        return event;
    }

    private VEvent createRecurringEvent(ClassDTO classDTO) {
        // Convertir LocalDate y LocalTime a ZonedDateTime
        ZoneId zoneId = ZoneId.of("Europe/Madrid"); // Zona horaria específica
        LocalDateTime startDateTime = LocalDateTime.of(classDTO.getInitDate(), classDTO.getInitHour());
        LocalDateTime endDateTime = LocalDateTime.of(classDTO.getInitDate(), classDTO.getFinishHour());

        ZonedDateTime startZoned = startDateTime.atZone(zoneId);
        ZonedDateTime endZoned = endDateTime.atZone(zoneId);

        // Crear evento
        VEvent event = new VEvent(startZoned, endZoned, classDTO.getGroup() + " - " + classDTO.getSubject());

        String uniqueId = java.util.UUID.randomUUID().toString() + "@calendarugr";
        event.add(new Uid(uniqueId));

        event.add(new Description("Profesores: " + classDTO.getTeachers()));
        event.add(new Location(classDTO.getClassroom()));
        try {
            URI subjectUri = new URI(classDTO.getSubjectUrl());
            event.add(new Url(subjectUri));
        } catch (Exception e) {
            System.err.println("URL inválida: " + classDTO.getSubjectUrl());
        }

        Recur<ZonedDateTime> recur = new Recur.Builder<ZonedDateTime>()
                .frequency(Frequency.WEEKLY)
                .until(classDTO.getFinishDate().atStartOfDay(ZoneId.of("UTC"))) 
                .dayList(Collections.singletonList(mapDayOfWeek(classDTO.getDay())))
                .build();

        RRule<ZonedDateTime> rrule = new RRule<>(recur);
        event.add(rrule);

        return event;
    }

}