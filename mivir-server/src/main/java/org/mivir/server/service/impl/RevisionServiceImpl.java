package org.mivir.server.service.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.mivir.api.BooleanMetadata;
import org.mivir.api.EntityCreateModification;
import org.mivir.api.EntityDeleteModification;
import org.mivir.api.EntityModification;
import org.mivir.api.EntityUpdateModification;
import org.mivir.api.Revision;
import org.mivir.api.StringMetadata;
import org.mivir.api.WritableMetadata;
import org.mivir.server.service.RevisionService;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.Date;
import java.util.Map;

@Service
public class RevisionServiceImpl implements RevisionService {

    private static final Logger LOG = LogManager.getLogger(RevisionServiceImpl.class);

    private GraphTraversalSource graph;

    RevisionServiceImpl(GraphTraversalSource traversalSource) {
        this.graph = traversalSource;
    }

    @Override
    public void executeRevision(Principal principal, Revision revision) {
        var transaction = graph.tx();
        for (EntityModification modification: revision.getEntities()) {
            if (modification instanceof EntityCreateModification create) {
                LOG.info("Creating node of type {}", create.getType());
                GraphTraversal<Vertex, Vertex> traversal = graph.addV(create.getType());
                traversal = traversal
                        .property("Created By", principal.getName())
                        .property("Created Date", new Date());
                traversal = applyProperties(traversal, create.getProperties());
                traversal.next();
            } else if (modification instanceof EntityUpdateModification update) {
                GraphTraversal<Vertex, Vertex> traversal = graph.V(update.getId());
                traversal = applyProperties(traversal, update.getProperties());
                traversal.next();
            } else if (modification instanceof EntityDeleteModification delete) {
                GraphTraversal<Vertex, Vertex> traversal = graph.V(delete.getId()).drop();
                traversal.next();
            }
        }
        transaction.commit();
    }

    private GraphTraversal<Vertex, Vertex> applyProperties(GraphTraversal<Vertex, Vertex> traversal, Map<String, WritableMetadata> properties) {
        for (Map.Entry<String, WritableMetadata> entry: properties.entrySet()) {
            String propertyName = entry.getKey();
            WritableMetadata metadata = entry.getValue();
            if (metadata instanceof StringMetadata s) {
                traversal = traversal.property(propertyName, s.getValue());
            } else if (metadata instanceof BooleanMetadata b) {
                traversal = traversal.property(propertyName, b.getValue());
            }
        }
        return traversal;
    }

}
