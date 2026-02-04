package com.example.notacompare.controller;

import com.example.notacompare.service.ExcelCompareService;
import com.example.notacompare.service.ExcelCompareService.CompareResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;

@Controller
public class CompareController {

    @Autowired
    private ExcelCompareService compareService;

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @PostMapping("/compare")
    public String compare(@RequestParam("sefaz") MultipartFile sefaz,
                          @RequestParam("sistema") MultipartFile sistema,
                          Model model) {
        if (sefaz == null || sefaz.isEmpty() || sistema == null || sistema.isEmpty()) {
            model.addAttribute("error", "Envie os dois arquivos: sefaz e sistema.");
            return "index";
        }

        try {
            CompareResult res = compareService.compare(sefaz.getInputStream(), sistema.getInputStream());
            model.addAttribute("onlyInSefaz", res.onlyInSefaz);
            model.addAttribute("onlyInSistema", res.onlyInSistema);
            return "result";
        } catch (Exception e) {
            model.addAttribute("error", "Erro ao processar arquivos: " + e.getMessage());
            return "index";
        }
    }
}
