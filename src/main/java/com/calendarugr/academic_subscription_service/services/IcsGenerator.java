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
import java.util.List;

import com.calendarugr.academic_subscription_service.dtos.ClassDTO;

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

    private VEvent createRecurringEvent(ClassDTO classDTO) {
        // Convertir LocalDate y LocalTime a ZonedDateTime
        ZoneId zoneId = ZoneId.of("Europe/Madrid"); // Zona horaria específica
        LocalDateTime startDateTime = LocalDateTime.of(classDTO.getInitDate(), classDTO.getInitHour());
        LocalDateTime endDateTime = LocalDateTime.of(classDTO.getInitDate(), classDTO.getFinishHour());

        ZonedDateTime startZoned = startDateTime.atZone(zoneId);
        ZonedDateTime endZoned = endDateTime.atZone(zoneId);

        // Crear evento
        VEvent event = new VEvent(startZoned, endZoned, classDTO.getGroup() + " - " + classDTO.getSubject());

        System.out.println("Event: " + event);

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

        System.out.println("Event: " + event);

        // Configurar la regla de recurrencia semanal con `UNTIL` en formato UTC
        Recur recur = new Recur.Builder()
                .frequency(Frequency.WEEKLY)
                .until(classDTO.getFinishDate()) // Ahora es LocalDate
                .dayList(Collections.singletonList(mapDayOfWeek(classDTO.getDay())))
                .build();

        RRule rrule = new RRule(recur);
        event.add(rrule);

        System.out.println("Event: " + event);

        return event;
    }

}