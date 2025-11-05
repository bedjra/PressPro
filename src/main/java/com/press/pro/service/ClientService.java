//package com.press.pro.service;
//
//import com.press.pro.Entity.Client;
//import com.press.pro.repository.ClientRepository;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//import java.util.Optional;
//
//@Service
//public class ClientService {
//
//    private final ClientRepository clientRepository;
//
//    public ClientService(ClientRepository clientRepository) {
//        this.clientRepository = clientRepository;
//    }
//
//    public List<Client> getAllClients() {
//        return clientRepository.findAll();
//    }
//
//    public Optional<Client> getClientById(Long id) {
//        return clientRepository.findById(id);
//    }
//
//    public Client addClient(Client client) {
//        return clientRepository.save(client);
//    }
//
//    public Client updateClient(Long id, Client clientDetails) {
//        return clientRepository.findById(id)
//                .map(existing -> {
//                    existing.setNom(clientDetails.getNom());
//                    existing.setTelephone(clientDetails.getTelephone());
//                    existing.setEmail(clientDetails.getEmail());
//                    existing.setAdresse(clientDetails.getAdresse());
//                    return clientRepository.save(existing);
//                })
//                .orElseThrow(() -> new RuntimeException("Client non trouvé"));
//    }
//
//    public void deleteClient(Long id) {
//        clientRepository.deleteById(id);
//    }
//
//    public List<Client> searchClients(String keyword) {
//        return clientRepository.searchByNomOrTelephone(keyword);
//    }
//
//
//}
//
