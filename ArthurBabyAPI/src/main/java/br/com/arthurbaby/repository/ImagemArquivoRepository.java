package br.com.arthurbaby.repository;

import br.com.arthurbaby.entity.ImagemArquivo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImagemArquivoRepository extends JpaRepository<ImagemArquivo, String> {
}
