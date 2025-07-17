package com.loopers.domain.point;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QPointModel is a Querydsl query type for PointModel
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPointModel extends EntityPathBase<PointModel> {

    private static final long serialVersionUID = 1443281032L;

    public static final QPointModel pointModel = new QPointModel("pointModel");

    public final com.loopers.domain.QBaseEntity _super = new com.loopers.domain.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.ZonedDateTime> createdAt = _super.createdAt;

    //inherited
    public final DateTimePath<java.time.ZonedDateTime> deletedAt = _super.deletedAt;

    //inherited
    public final NumberPath<Long> id = _super.id;

    public final NumberPath<Long> point = createNumber("point", Long.class);

    //inherited
    public final DateTimePath<java.time.ZonedDateTime> updatedAt = _super.updatedAt;

    public final NumberPath<Long> userId = createNumber("userId", Long.class);

    public QPointModel(String variable) {
        super(PointModel.class, forVariable(variable));
    }

    public QPointModel(Path<? extends PointModel> path) {
        super(path.getType(), path.getMetadata());
    }

    public QPointModel(PathMetadata metadata) {
        super(PointModel.class, metadata);
    }

}

