package com.pp1.salve.model.loja;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.pp1.salve.exceptions.UnauthorizedAccessException;
import com.pp1.salve.kc.KeycloakService;
import com.pp1.salve.minio.MinIOInterfacing;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class LojaService {
  private static final String LOJA = "loja";
  private static final String LOJA_IMAGE = "lojaImage";
  private final MinIOInterfacing minIOInterfacing;

  private final LojaRepository repository;

  private final SegmentoLojaRepository segmentoLojaRepository;

  private final KeycloakService keycloakService;

  public List<Loja> findMyLojas(Authentication authentication) throws Exception {
    List<Loja> lojas = repository.findByCriadoPorId(authentication.getName());
    for (Loja l : lojas) {
      l = monta(l);
    }
    return lojas;
  }

  public Page<Loja> findAll(Pageable pageable) throws Exception {
    Page<Loja> loja = repository.findAll(pageable);
    for (Loja l : loja) {
      l = monta(l);
    }
    return loja;
  }

  public Page<Loja> findAll(Pageable pageable, double lat, double longi) throws Exception {
    Page<Loja> loja = repository.findAll(pageable, lat, longi);
    for (Loja l : loja) {
      l = monta(l, lat, longi);
    }
    return loja;
  }

  public Page<Loja> findAllSegmento(Long id, Pageable pageable) throws Exception {
    Page<Loja> loja = repository.findBySegmentoLojaId(id, pageable);
    for (Loja l : loja) {
      l = monta(l);
    }
    return loja;
  }

  public Page<Loja> findAllSegmento(Long id, Pageable pageable, double lat, double longi) throws Exception {
    Page<Loja> loja = repository.findBySegmentoLojaId(id, pageable, lat, longi);
    for (Loja l : loja) {
      l = monta(l, lat, longi);
    }
    return loja;
  }

  public Loja findById(Long id) {
    return repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Loja não encontrada"));
  }

  public Loja save(Loja loja, MultipartFile file, Authentication authentication) throws Exception {

    if (this.findMyLojas(authentication).size() == 0) {
      keycloakService.addRoleToUser(authentication.getName(), "dono_de_loja");

      SegmentoLoja segmentoLoja = segmentoLojaRepository.findById(loja.getSegmentoLoja().getId())
          .orElseThrow(() -> new EntityNotFoundException(
              "Segmento de Loja não encontrado com ID: " + loja.getSegmentoLoja().getId()));
      loja.setSegmentoLoja(segmentoLoja);
      Loja lojaSalva = repository.save(loja);
      lojaSalva.setImage(minIOInterfacing.uploadFile(lojaSalva.getId() + LOJA, LOJA_IMAGE, file));
      return repository.save(lojaSalva);
    } else {
      throw new UnauthorizedAccessException("Você já possui uma loja cadastrada.");
    }
  }

  @Transactional(rollbackFor = Exception.class)
  public Loja update(Long id, Loja loja, MultipartFile file, Authentication authentication) throws Exception {
    Optional<Loja> lojaOptional = repository.findById(id);
    if (lojaOptional.isEmpty()) {
      throw new EntityNotFoundException("Loja não encontrada");
    } else {
      if (!lojaOptional.get().getCriadoPor().getId().equals(authentication.getName()))
        throw new UnauthorizedAccessException("Você não tem autoridade de modificar esta loja.");
    }
    final long ids = loja.getSegmentoLoja().getId();
    SegmentoLoja segmentoLoja = segmentoLojaRepository.findById(ids)
        .orElseThrow(() -> new EntityNotFoundException(
            "Segmento de Loja não encontrado com ID: " + ids));

    Loja lojaLocal = lojaOptional.get();
    lojaLocal.setBairro(loja.getBairro());
    lojaLocal.setCidade(loja.getCidade());
    lojaLocal.setDescricao(loja.getDescricao());
    lojaLocal.setEstado(loja.getEstado());
    lojaLocal.setLatitude(loja.getLatitude());
    lojaLocal.setLongitude(loja.getLongitude());
    lojaLocal.setNome(loja.getNome());
    lojaLocal.setNumero(loja.getNumero());
    lojaLocal.setRua(loja.getRua());
    lojaLocal.setSegmentoLoja(segmentoLoja);

    if (file != null) {
      log.info("File is not null");
      loja = repository.save(lojaLocal);
      loja.setImage(minIOInterfacing.uploadFile(getUniqueName(lojaLocal.getId()), LOJA_IMAGE, file));
      return loja;
    }
    return monta(repository.save(lojaLocal));
  }

  public void deleteById(Long id) {
    repository.deleteById(id);
  }

  public Loja monta(Loja loja) throws Exception {
    loja.setImage(minIOInterfacing.getSingleUrl(getUniqueName(loja.getId()), LOJA_IMAGE));
    return loja;
  }

  public Loja monta(Loja loja, double lati, double longi) throws Exception {
    loja.setImage(minIOInterfacing.getSingleUrl(getUniqueName(loja.getId()), LOJA_IMAGE));
    loja.setDeliveryTime(deliveryTimeCalculator(loja, lati, longi));
    return loja;
  }

  public double deliveryTimeCalculator(Loja loja, double latitude, double longitude) {
    double lojaLatitude = loja.getLatitude();
    double lojaLongitude = loja.getLongitude();

    return calculateTravelTime(lojaLatitude, lojaLongitude, latitude, longitude);
  }

  public static double calculateTravelTime(double storeLatitude, double storeLongitude, double userLatitude,
      double userLongitude) {
    // Raio médio da Terra em quilômetros
    final double EARTH_RADIUS_KM = 6371.0;

    // Converter as latitudes e longitudes de graus para radianos
    double latDistance = Math.toRadians(storeLatitude - userLatitude);
    double lonDistance = Math.toRadians(storeLongitude - userLongitude);

    double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
        + Math.cos(Math.toRadians(userLatitude)) * Math.cos(Math.toRadians(storeLatitude))
            * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

    // Distância em quilômetros
    double distance = EARTH_RADIUS_KM * c;

    // Tempo de viagem em horas
    double travelTimeHours = distance / 15;

    // Converter o tempo de viagem para minutos
    return travelTimeHours * 60;
  }

  private String getUniqueName(Long id) {
    return id + "" + LOJA;
  }
}
