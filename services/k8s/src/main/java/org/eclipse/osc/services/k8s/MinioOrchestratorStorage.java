package org.eclipse.osc.services.k8s;

import io.minio.*;
import io.minio.errors.ErrorResponseException;
import io.minio.messages.Item;
import lombok.extern.slf4j.Slf4j;
import org.apache.karaf.minho.boot.service.ConfigService;
import org.apache.karaf.minho.boot.service.ServiceRegistry;
import org.apache.karaf.minho.boot.spi.Service;
import org.eclipse.osc.orchestrator.OrchestratorStorage;

import java.io.ByteArrayInputStream;
import java.util.HashSet;
import java.util.Set;

@Slf4j
public class MinioOrchestratorStorage implements OrchestratorStorage, Service {

    private static final String BUCKET_NAME = "osc-orchestrator";

    private MinioClient minioClient;

    @Override
    public String name() {
        return "osc-minio-storage";
    }

    @Override
    public void onRegister(ServiceRegistry serviceRegistry) {
        ConfigService configService = serviceRegistry.get(ConfigService.class);
        String minioEndpoint = configService.getProperty("osc.minio.endpoint", "http://localhost:30090");
        String minioAccessKey = configService.getProperty("osc.minio.accessKey", "osc");
        String minioSecretKey = configService.getProperty("osc.minio.secretKey", "oscoscosc");
        minioClient = MinioClient.builder().endpoint(minioEndpoint)
                .credentials(minioAccessKey, minioSecretKey)
                .build();

        try {
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(BUCKET_NAME).build());
            if (!found) {
                log.info("Creating {} bucket", BUCKET_NAME);
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(BUCKET_NAME).build());
            } else {
                log.info("{} bucket already exists", BUCKET_NAME);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Can't create " + BUCKET_NAME + " bucket in Minio", e);
        }
    }


    @Override
    public void store(String sid) {
        if (!exists(sid)) {
            try (ByteArrayInputStream bais = new ByteArrayInputStream(sid.getBytes("UTF-8"))) {
                minioClient.putObject(PutObjectArgs.builder().bucket(BUCKET_NAME).object(sid)
                    .stream(bais, bais.available(), -1).build());
            } catch (Exception e) {
                throw new IllegalStateException("Can't store " + sid + " in Minio", e);
            }
        }
    }

    @Override
    public void store(String sid, String pluginName, String key, String value) {

    }

    @Override
    public String getKey(String sid, String pluginName, String key) {
        return "";
    }

    @Override
    public boolean exists(String sid) {
        try {
            minioClient.getObject(GetObjectArgs.builder().bucket(BUCKET_NAME).object(sid).build());
            return true;
        } catch (ErrorResponseException e) {
            return false;
        } catch (Exception e) {
            throw new IllegalStateException("Can't check if " + sid + " exists in Minio", e);
        }
    }

    @Override
    public Set<String> services() {
        Set<String> services = new HashSet<>();
        try {
            Iterable<Result<Item>> results = minioClient.listObjects(ListObjectsArgs.builder().bucket(BUCKET_NAME).build());
            for (Result<Item> result : results) {
                Item item = result.get();
                services.add(item.objectName());
            }
        } catch (Exception e) {
            throw new IllegalStateException("Can't get the services list from Minio", e);
        }
        return services;
    }

    @Override
    public void remove(String sid) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder().bucket(BUCKET_NAME).object(sid).build());
        } catch (Exception e) {
            throw new IllegalStateException("Can't remove " + sid + " from Minio", e);
        }
    }

}
