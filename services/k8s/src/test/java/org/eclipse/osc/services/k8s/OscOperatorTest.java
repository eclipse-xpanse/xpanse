package org.eclipse.osc.services.k8s;

import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.NamespaceList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class OscOperatorTest {

    @Test
    @Disabled("Need Kubernetes cluster")
    public void simpleRun() throws Exception {
        KubernetesClient client = new KubernetesClientBuilder().build();

        NonNamespaceOperation<Namespace, NamespaceList, Resource<Namespace>> namespaces = client.namespaces();
        namespaces.list().getItems().stream().forEach((namespace) -> {
            System.out.println(namespace.getMetadata().getName());
        });

        client.pods().inNamespace("osc");

        // Minho minho = Minho.builder().build().start();

    }

}
