package org.mivir.server.service.impl;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.mivir.server.repository.GraphRepository;
import org.mivir.server.service.SecurityService;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class SecurityServiceImpl implements SecurityService {

    private GraphTraversalSource g;

    private GraphRepository graphRepository;

    SecurityServiceImpl(GraphTraversalSource source, GraphRepository graphRepository) {
        this.g = source;
        this.graphRepository = graphRepository;
    }

    @Override
    public String userForAccessToken(String token) {

        Map<Object, Object> userProps = graphRepository.vertexWithProperties("mivir:user", Map.of("accessToken", token));
        if (userProps != null) {
            return (String) userProps.get("name");
        } else {
            return null;
        }

        //return g.V().hasLabel("mivir:user").has("accessToken", token).hasNext();
    }
}
