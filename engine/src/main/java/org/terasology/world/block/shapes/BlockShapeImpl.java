/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.world.block.shapes;

import org.terasology.physics.shapes.CollisionShape;
import com.google.common.collect.Maps;
import org.terasology.assets.AssetType;
import org.terasology.assets.ResourceUrn;
import org.terasology.math.Pitch;
import org.terasology.math.Roll;
import org.terasology.math.Rotation;
import org.terasology.math.Side;
import org.terasology.math.Yaw;
import org.terasology.math.geom.Vector3f;
import org.terasology.utilities.collection.EnumBooleanMap;
import org.terasology.world.block.BlockPart;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 */
public class BlockShapeImpl extends BlockShape {

    private String displayName;
    private List<BlockMeshPart> meshParts = new ArrayList<>();
    private EnumMap<Side, List<BlockMeshPart>> meshBySide = new EnumMap<>(Side.class);
    private EnumBooleanMap<Side> fullSide = new EnumBooleanMap<>(Side.class);
    private CollisionShape baseCollisionShape;
    private Vector3f baseCollisionOffset = new Vector3f();
    private boolean yawSymmetric;
    private boolean pitchSymmetric;
    private boolean rollSymmetric;

    private Map<Rotation, CollisionShape> collisionShape = Maps.newHashMap();

    public BlockShapeImpl(ResourceUrn urn, AssetType<?, BlockShapeData> assetType, BlockShapeData data) {
        super(urn, assetType);
        reload(data);
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    /**
     * @return All mesh parts contained by the block
     */
    @Override
    public List<BlockMeshPart> getMeshParts() {
        return meshParts;
    }

    @Override
    public List<BlockMeshPart> getMeshParts(Side side) {
        return meshBySide.get(side);
    }

    @Override
    public boolean isBlockingSide(Side side) {
        return fullSide.get(side);
    }

    @Override
    protected void doReload(BlockShapeData data) {
        collisionShape.clear();
        displayName = data.getDisplayName();
        this.meshParts.addAll(data.getMeshParts());

        for (Side side : Side.getAllSides()) {
            this.fullSide.put(side, data.isBlockingSide(side));
            this.meshBySide.put(side, data.getMeshParts(side));
        }
        this.baseCollisionShape = data.getCollisionShape();
        this.baseCollisionOffset.set(data.getCollisionOffset());
        collisionShape.put(Rotation.none(), baseCollisionShape);

        yawSymmetric = data.isYawSymmetric();
        pitchSymmetric = data.isPitchSymmetric();
        rollSymmetric = data.isRollSymmetric();
    }

    @Override
    public CollisionShape getCollisionShape(Rotation rot) {
        Rotation simplifiedRot = applySymmetry(rot);
        CollisionShape result = collisionShape.get(simplifiedRot);
        if (result == null && baseCollisionShape != null) {
            result = baseCollisionShape.rotate(simplifiedRot.getQuat4f());
            collisionShape.put(simplifiedRot, result);
        }
        return result;
    }

    @Override
    public Vector3f getCollisionOffset(Rotation rot) {
        Rotation simplifiedRot = applySymmetry(rot);
        if (simplifiedRot.equals(Rotation.none())) {
            return new Vector3f(baseCollisionOffset);
        }
        return simplifiedRot.getQuat4f().rotate(baseCollisionOffset, new Vector3f());
    }

    @Override
    public boolean isCollisionYawSymmetric() {
        return yawSymmetric;
    }

    private Rotation applySymmetry(Rotation rot) {
        return Rotation.rotate(yawSymmetric ? Yaw.NONE : rot.getYaw(), pitchSymmetric ? Pitch.NONE : rot.getPitch(), rollSymmetric ? Roll.NONE : rot.getRoll());
    }

}

