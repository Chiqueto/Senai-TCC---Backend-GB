package com.senai.gestao_beneficios.DTO.disponibilidade;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;

@Component
public class DisponibilidadeMapper {

    public DayOfWeek toDayOfWeek(int diaDaSemana){
        return switch (diaDaSemana) {
            case 0 -> DayOfWeek.SUNDAY;
            case 1 -> DayOfWeek.MONDAY;
            case 2 -> DayOfWeek.TUESDAY;
            case 3 -> DayOfWeek.WEDNESDAY;
            case 4 -> DayOfWeek.THURSDAY;
            case 5 -> DayOfWeek.FRIDAY;
            case 6 -> DayOfWeek.SATURDAY;
            default -> null;
        };
    }

    public int toInteger(DayOfWeek diaDaSemana){
        return switch (diaDaSemana) {
            case DayOfWeek.SUNDAY -> 0;
            case DayOfWeek.MONDAY -> 1;
            case DayOfWeek.TUESDAY -> 2;
            case DayOfWeek.WEDNESDAY -> 3;
            case DayOfWeek.THURSDAY -> 4;
            case DayOfWeek.FRIDAY -> 5;
            case DayOfWeek.SATURDAY -> 6;
        };
    }

}
