package com.datech.mvp.controller;

import com.datech.mvp.model.Client;
import com.datech.mvp.repository.ClientRepository;
import com.datech.mvp.service.CrudService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clients")
public class ClientController {
    private final ClientRepository repository;
    private final CrudService crudService;

    public ClientController(ClientRepository repository, CrudService crudService) {
        this.repository = repository;
        this.crudService = crudService;
    }

    @GetMapping
    public List<Client> all() {
        return crudService.findAll(repository);
    }

    @PostMapping
    public Client create(@Valid @RequestBody Client client) {
        return crudService.save(repository, client);
    }

    @GetMapping("/{id}")
    public Client one(@PathVariable Long id) {
        return crudService.findById(repository, id);
    }

    @PutMapping("/{id}")
    public Client update(@PathVariable Long id, @Valid @RequestBody Client client) {
        client.setId(id);
        return crudService.save(repository, client);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        crudService.delete(repository, id);
    }
}
