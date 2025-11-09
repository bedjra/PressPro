package com.press.pro.service;

import com.press.pro.Dto.ClientDto;
import com.press.pro.Entity.Client;
import com.press.pro.Entity.Utilisateur;
import com.press.pro.repository.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClientService {

    @Autowired
    private ClientRepository clientRepository;

    // 🔹 Convertir Client → ClientDto
    private ClientDto toDto(Client client) {
        ClientDto dto = new ClientDto();
        dto.setId(client.getId());
        dto.setNom(client.getNom());
        dto.setTelephone(client.getTelephone());
        dto.setAdresse(client.getAdresse());
        dto.setStatutClient(client.getStatutClient());
        dto.setDate(client.getDate());
        return dto;
    }

    // 🔹 Convertir ClientDto → Client
    private Client toEntity(ClientDto dto) {
        Client client = new Client();
        client.setNom(dto.getNom());
        client.setTelephone(dto.getTelephone());
        client.setAdresse(dto.getAdresse());
        return client;
    }

    // ✅ Créer un client
    public ClientDto createClient(ClientDto clientDto, Utilisateur utilisateurConnecte) {
        Client client = toEntity(clientDto);
        client.setPressing(utilisateurConnecte.getPressing());
        Client saved = clientRepository.save(client);
        return toDto(saved);
    }

    // ✅ Lister les clients du pressing connecté
    public List<ClientDto> getClients(Utilisateur utilisateurConnecte) {
        Long pressingId = utilisateurConnecte.getPressing().getId();
        return clientRepository.findAllByPressing(utilisateurConnecte.getPressing())
                .stream()
                .filter(c -> c.getPressing().getId().equals(pressingId))
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // ✅ Récupérer un client par ID
    public ClientDto getClientById(Long id, Utilisateur utilisateurConnecte) {
        Client client = clientRepository.findDistinctByIdWithPressing(id)
                .orElseThrow(() -> new RuntimeException("Client non trouvé"));

        if (!client.getPressing().getId().equals(utilisateurConnecte.getPressing().getId())) {
            throw new RuntimeException("Accès refusé : client d'un autre pressing");
        }
        return toDto(client);
    }

    // ✅ Mettre à jour un client
    public ClientDto updateClient(Long id, ClientDto updatedDto, Utilisateur utilisateurConnecte) {
        Client client = clientRepository.findDistinctByIdWithPressing(id)
                .orElseThrow(() -> new RuntimeException("Client non trouvé"));

        if (!client.getPressing().getId().equals(utilisateurConnecte.getPressing().getId())) {
            throw new RuntimeException("Accès refusé : client d'un autre pressing");
        }

        client.setNom(updatedDto.getNom());
        client.setTelephone(updatedDto.getTelephone());
        client.setAdresse(updatedDto.getAdresse());
        if (updatedDto.getStatutClient() != null) {
            client.setStatutClient(updatedDto.getStatutClient());
        }

        Client saved = clientRepository.save(client);
        return toDto(saved);
    }

    // ✅ Supprimer un client
    public void deleteClient(Long id, Utilisateur utilisateurConnecte) {
        Client client = clientRepository.findDistinctByIdWithPressing(id)
                .orElseThrow(() -> new RuntimeException("Client non trouvé"));

        if (!client.getPressing().getId().equals(utilisateurConnecte.getPressing().getId())) {
            throw new RuntimeException("Accès refusé : client d'un autre pressing");
        }

        clientRepository.delete(client);
    }
}
