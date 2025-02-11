package ai.basic.x1.adapter.dto;

import ai.basic.x1.entity.enums.DataAnnotationStatusEnum;
import ai.basic.x1.entity.enums.DataStatusEnum;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author fyb
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataInfoDTO {

    /**
     * Data id
     */
    private Long id;

    /**
     * Dataset id
     */
    private Long datasetId;

    /**
     * Data name
     */
    private String name;

    /**
     * Content (folder path, version information)
     */
    private List<FileNodeDTO> content;

    /**
     * Data status INVALID,VALID
     */
    private DataStatusEnum status;

    /**
     * Data annotation status ANNOTATED, NOT_ANNOTATED, INVALID
     */
    private DataAnnotationStatusEnum annotationStatus;

    /**
     * Locked Person's Name
     */
    private String lockedBy;


    @Data
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    public static class FileNodeDTO {
        /**
         * File name
         */
        private String name;

        /**
         * File id
         */
        private Long fileId;

        /**
         * File type(directory,file)
         */
        private String type;

        /**
         * File information
         */
        private FileDTO file;

        /**
         * Sub file
         */
        private List<FileNodeDTO> files;

        /**
         * Folder Type (Identifies what type of file is below)
         */
        private String directoryType;

        public String getDirectoryType() {
            if (CollectionUtil.isNotEmpty(files)) {
                var file = CollectionUtil.getFirst(files).getFile();
                return ObjectUtil.isNotNull(file) ? file.getType() : null;
            }
            return directoryType;
        }

    }
}
