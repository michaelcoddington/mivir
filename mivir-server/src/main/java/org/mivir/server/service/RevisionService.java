package org.mivir.server.service;

import org.mivir.api.Revision;

import java.security.Principal;

public interface RevisionService {

    void executeRevision(Principal principal, Revision revision);

}
