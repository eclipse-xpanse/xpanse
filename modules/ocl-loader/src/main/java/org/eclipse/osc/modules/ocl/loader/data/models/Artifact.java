package org.eclipse.osc.modules.ocl.loader.data.models;

import jakarta.annotation.Nonnull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class Artifact extends RuntimeBase {

    @Nonnull
    private String name;
    private String base;
    private String type;
    private List<String> provisioners;

}
