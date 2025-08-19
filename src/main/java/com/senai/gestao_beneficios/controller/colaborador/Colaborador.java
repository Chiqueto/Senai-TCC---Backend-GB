package com.senai.gestao_beneficios.controller.colaborador;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/colaborador")
public class Colaborador {
    @GetMapping("/{id}")
    public String getColaboradorById(@PathVariable String id ){
        return "Olá mundo!";
    }
}
