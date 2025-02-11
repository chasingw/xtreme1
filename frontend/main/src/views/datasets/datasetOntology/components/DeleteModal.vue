<template>
  <BasicModal
    v-bind="$attrs"
    @register="register"
    :title="t('business.class.deleteClass')"
    :ok-text="t('common.confirmText')"
    @ok="handleDelete"
    centered
    :okButtonProps="{
      danger: true,
      loading: isLoading,
    }"
  >
    <div class="info-text text-center">
      <div>{{ t('business.class.deleteSure') + ' “' + name + '” ?' }}</div>
    </div>
  </BasicModal>
</template>
<script lang="ts" setup>
  import { ref, inject } from 'vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { handleMutiTabAction } from './modal-components/utils';
  import { ClassTypeEnum } from '/@/api/business/model/ontologyClassesModel';
  import { deleteClassApi, deleteClassificationApi } from '/@/api/business/ontologyClasses';
  import {
    deleteClassApi as deleteDatasetClassApi,
    deleteClassificationApi as deleteDatasetClassificationApi,
  } from '/@/api/business/datasetOntology';

  const { createMessage } = useMessage();
  const handleRefresh = inject('handleRefresh', Function, true);

  const [register, { closeModal }] = useModalInner();
  const { t } = useI18n();
  const props = defineProps<{
    id: string | number;
    name: string;
    activeTab: ClassTypeEnum;
    isCenter?: boolean;
  }>();

  /** Confirm Delete */
  const isLoading = ref<boolean>(false);
  const handleDelete = async () => {
    isLoading.value = true;
    handleMutiTabAction(
      props.activeTab,
      async () => {
        try {
          if (props.isCenter) {
            await deleteClassApi({ id: props.id });
          } else {
            await deleteDatasetClassApi({ id: props.id });
          }

          const successText =
            t('business.class.class') + ` "${props.name}" ` + t('business.class.hasDeleted');
          createMessage.success(successText);

          isLoading.value = false;
          closeModal();
          handleRefresh();
        } catch (error) {
          isLoading.value = false;
          console.log('error', error);
        }
      },
      async () => {
        try {
          if (props.isCenter) {
            await deleteClassificationApi({ id: props.id });
          } else {
            await deleteDatasetClassificationApi({ id: props.id });
          }

          const successText =
            t('business.class.classification') +
            ` "${props.name}" ` +
            t('business.class.hasDeleted');
          createMessage.success(successText);

          isLoading.value = false;
          closeModal();
          handleRefresh();
        } catch (error) {
          isLoading.value = false;
          console.log('error', error);
        }
      },
    );
  };
</script>
<style scoped>
  .info-text {
    width: 368px;
    margin: 0 auto 10px;
    padding-top: 100px;
    color: #333;
    font-size: 16px;
  }
</style>
