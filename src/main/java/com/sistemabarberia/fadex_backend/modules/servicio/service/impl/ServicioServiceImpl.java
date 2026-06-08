package com.sistemabarberia.fadex_backend.modules.servicio.service.impl;

import com.sistemabarberia.fadex_backend.commons.exception.BusinessException;
import com.sistemabarberia.fadex_backend.commons.exception.ResourceNotFoundException;
import com.sistemabarberia.fadex_backend.commons.storage.FileStorageService;
import com.sistemabarberia.fadex_backend.modules.categoria.entity.Categoria;
import com.sistemabarberia.fadex_backend.modules.categoria.repository.CategoriaRepository;
import com.sistemabarberia.fadex_backend.modules.servicio.dto.request.ServicioRequestDTO;
import com.sistemabarberia.fadex_backend.modules.servicio.dto.response.ServicioResponseDTO;
import com.sistemabarberia.fadex_backend.modules.servicio.entity.Servicio;
import com.sistemabarberia.fadex_backend.modules.servicio.mapper.ServicioMapper;

import com.sistemabarberia.fadex_backend.modules.servicio.repository.ServicioRepository;
import com.sistemabarberia.fadex_backend.modules.servicio.service.IServicioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ServicioServiceImpl implements IServicioService {

    private final ServicioRepository servicioRepository;
    private final CategoriaRepository categoriaRepository;
    private final ServicioMapper servicioMapper;
    private final FileStorageService fileStorageService;
    private static final List<String> TIPOS_IMAGEN = List.of("image/jpeg", "image/png", "image/webp");


    @Override
    public ServicioResponseDTO crear(ServicioRequestDTO dto, List<MultipartFile> archivos) {

        Categoria categoria = categoriaRepository.findById(dto.getCategoriaId())
                .orElseThrow(() -> new BusinessException(
                        "La categoría con ID " + dto.getCategoriaId() + " no existe",
                        HttpStatus.BAD_REQUEST
                ));

        if (servicioRepository.existsByNombre(dto.getNombre().trim())) {
            throw new BusinessException(
                    "El servicio ya existe",
                    HttpStatus.BAD_REQUEST
            );
        }

        Servicio servicio = servicioMapper.toEntity(dto);
        servicio.setCategoria(categoria);

        List<String> urls = new ArrayList<>();

        List<MultipartFile> archivosValidos = filtrarArchivosNoVacios(archivos);

        if (!archivosValidos.isEmpty()) {

            for (MultipartFile file : archivosValidos) {

                validarArchivoImagen(file);

                String url = fileStorageService.guardarArchivo(
                        file,
                        "servicios",
                        TIPOS_IMAGEN
                );

                urls.add(url);
            }
        }

        servicio.setUrlsMultimedia(urls);

        Servicio guardado = servicioRepository.save(servicio);

        return servicioMapper.toResponse(guardado);
    }

    @Override
    public List<ServicioResponseDTO> listar() {
        return servicioMapper.toResponseList(servicioRepository.findAll());
    }

    @Override
    public List<ServicioResponseDTO> listarPorCategoria(Long categoriaId) {

        if (!categoriaRepository.existsById(categoriaId)) {
            throw new ResourceNotFoundException("Categoría no encontrada");
        }

        return servicioMapper.toResponseList(
                servicioRepository.findByCategoriaId_Id(categoriaId)
        );
    }

    @Override
    public ServicioResponseDTO obtenerPorId(Long id) {
        Servicio servicio = servicioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado"));

        return servicioMapper.toResponse(servicio);
    }

    @Override
    public ServicioResponseDTO actualizar(Long id, ServicioRequestDTO dto) {

        Servicio servicio = servicioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado"));

        if (!servicio.getNombre().equals(dto.getNombre()) &&
                servicioRepository.existsByNombre(dto.getNombre())) {
            throw new BusinessException("Servicio ya existe", HttpStatus.BAD_REQUEST);
        }

        Categoria categoria = categoriaRepository.findById(dto.getCategoriaId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada"));

        servicioMapper.updateEntityFromDto(dto, servicio);

        servicio.setCategoria(categoria);

        Servicio actualizado = servicioRepository.save(servicio);

        return servicioMapper.toResponse(actualizado);
    }

    @Override
    public void eliminar(Long id) {

        if (!servicioRepository.existsById(id)) {
            throw new ResourceNotFoundException("Servicio no encontrado");
        }

        servicioRepository.deleteById(id);
    }
    private List<MultipartFile> filtrarArchivosNoVacios(List<MultipartFile> archivos) {
        if (archivos == null || archivos.isEmpty()) {
            return List.of();
        }
        List<MultipartFile> archivosValidos = new ArrayList<>();
        for (MultipartFile archivo : archivos) {
            if (archivo != null && !archivo.isEmpty()) {
                archivosValidos.add(archivo);
            }
        }
        return archivosValidos;
        
    }

    private void validarArchivoImagen(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("Archivo vacío", HttpStatus.BAD_REQUEST);
        }
        if (!TIPOS_IMAGEN.contains(file.getContentType())) {
            throw new BusinessException("Formato no permitido: " + file.getContentType(), HttpStatus.BAD_REQUEST);
        }
    }
}