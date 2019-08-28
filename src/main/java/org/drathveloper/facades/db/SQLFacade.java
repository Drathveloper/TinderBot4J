package org.drathveloper.facades.db;

import org.drathveloper.models.MatchList;
import org.drathveloper.models.UserBatch;

public interface SQLFacade {

    boolean isConnectionAvailable();

    void insertBatch(UserBatch batch);

    void updateMatches(MatchList matches);
}
