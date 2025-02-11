<template>
  <div class="basic-form">
    <FormHeader
      :dataSchema="props.dataSchema"
      :indexList="props.indexList"
      @back="handleBack"
      @del="handleDelete"
    />
    <BasicForm @register="registerForm" :showActionButtonGroup="false" hideRequiredMark>
      <template #inputType="{ model, field }">
        <Select v-model:value="model[field]" @change="handleChangeType">
          <Select.Option v-for="item in inputTypeList" :key="item.key" :value="item.value">
            <div class="img-tool">
              <img :src="item.img" alt="" />
              <span>{{ item.label }}</span>
            </div>
          </Select.Option>
        </Select>
      </template>
    </BasicForm>
    <Divider style="margin: 10px 0" />
    <OptionEditor
      v-if="isShow"
      type="options"
      v-model:showRequired="showEditorRequired"
      :dataSchema="data"
      :handleSet="handleSet"
      :handleAddIndex="handleAddIndex"
    />
    <FormDiscard
      :showModal="showDiscardModal"
      @cancel="handleCancelDiscard"
      @discard="handleConfirmDiscard"
    />
  </div>
</template>
<script lang="ts" setup>
  import { ref, unref, onMounted, watch, inject } from 'vue';
  import { Divider, Select } from 'ant-design-vue';
  import { BasicForm, useForm } from '/@/components/Form';
  import OptionEditor from './OptionEditor.vue';
  import FormHeader from './FormHeader.vue';
  import FormDiscard from './FormDiscard.vue';
  import emitter from 'tiny-emitter/instance';
  import { attributeBase } from './formSchemas';
  import { getSchema, handleMutiTabAction, setClassSchema, setSchema } from './utils';
  import { ClassTypeEnum, inputTypeEnum } from '/@/api/business/model/ontologyClassesModel';
  import { inputTypeList } from './data';

  const emits = defineEmits(['done', 'del', 'changeIndexList', 'createSave', 'valid']);
  const props = defineProps<{
    dataSchema?: any;
    handleSet?: Function;
    handleAddIndex?: Function;
    indexList?: number[];
    activeTab?: ClassTypeEnum;
  }>();

  const { handleSet, handleAddIndex } = unref(props);
  const data = ref<any>();
  data.value = getSchema(props.dataSchema, props.indexList);
  // There must be option under attrForm
  watch(data.value.options, (newData) => {
    if (newData.length > 0) {
      showEditorRequired.value = false;
    }
  });

  const isShow = ref<boolean>(true);

  /** FormHeader */
  // Return to the previous level, you need to judge the length of options
  const handleBack = () => {
    emitter.emit('handleSaveForm', {
      type: 'back',
    });
  };
  // Delete
  const handleDelete = () => {
    emits('del');
  };

  /** FormDiscard */
  // whether to display InnerDiscardModal
  const showDiscardModal = ref<boolean>(false);
  // Cancel Discard
  const handleCancelDiscard = () => {
    showDiscardModal.value = false;
  };
  // Confirm Discard
  const handleConfirmDiscard = () => {
    handleDelete();
  };
  // Whether to display the prompt information of Options
  // -- Required flag for optionEditor,
  const showEditorRequired = ref<boolean>(false);

  /** BasicForm */
  // Drop-down box toggle selection event
  const handleChangeType = (val) => {
    if (val === inputTypeEnum.TEXT) {
      isShow.value = false;
      // options need to be cleared
      props.handleSet &&
        props.handleSet({
          setType: 'update',
          setValue: { options: [] },
        });
      data.value = getSchema(props.dataSchema, props.indexList);
    } else {
      isShow.value = true;
      data.value = getSchema(props.dataSchema, props.indexList);
    }
    emitter.emit('handleSaveForm', { type: 'change' });
  };
  // register Form
  const [registerForm, { setFieldsValue, validate, getFieldsValue }] = useForm({
    schemas: attributeBase(),
  });
  // validation form - name
  const validateAttrForm = async () => {
    try {
      await validate();
      return true;
    } catch {
      return false;
    }
  };

  const isShowEdit = inject('isShowEdit');

  emitter.off('handleSaveForm');
  emitter.on('handleSaveForm', async (params?) => {
    console.log('Save Attribute Form', data);
    // validation form first
    const res = await validateAttrForm();

    if (res) {
      // Check the options field
      // -- not triggered by blur
      if (params?.type != 'blur') {
        // First, get the inputType value of the value of the current form
        const { type } = getFieldsValue();

        // If the type is not text and the current type is not change, the subsequent judgment will be made
        if (type != inputTypeEnum.TEXT && params?.type != 'change') {
          const { options } = data.value;
          if ((isShowEdit as any).value) {
            showDiscardModal.value = true;
            if (params?.type == 'tree') {
              emitter.emit('changeSelected', params.selectList);
            }
            return;
          } else if (options.length > 0) {
            showEditorRequired.value = false;
          } else {
            showEditorRequired.value = true;
            showDiscardModal.value = true;

            // If you click on the tree node, you need to prevent the tree selected node from changing
            if (params?.type == 'tree') {
              emitter.emit('changeSelected', params.selectList);
            }
            return;
          }
        }
      }

      // Save
      handleMutiTabAction(
        props.activeTab,
        () => {
          setClassSchema(props.dataSchema, props.indexList, {
            setType: 'update',
            setValue: getFieldsValue() as any,
          });
        },
        () => {
          setSchema(props.dataSchema, props.indexList, {
            setType: 'update',
            setValue: getFieldsValue() as any,
          });
        },
      );

      // If it's Go , go to index
      if (params?.type == 'go') {
        props.handleAddIndex && props.handleAddIndex(params?.index);
      }

      // If it is a tree data click, update the indexList value, to update the data on the right
      if (params?.type == 'tree') {
        emits('changeIndexList', params.indexList);
      }

      // If it returns, throw done
      if (params.type == 'back') {
        emits('done');
      }

      // create or save
      if (params.type == 'create') {
        emits('createSave', params.data);
      }
    } else {
      showDiscardModal.value = true;
      if (params?.type != 'blur') {
        showDiscardModal.value = true;
      }
      // Prevent tree selected node from changing
      if (params?.type == 'tree') {
        emitter.emit('changeSelected', params.selectList);
      }
    }
  });

  onMounted(() => {
    setFieldsValue({
      ...data.value,
    });
    handleChangeType(data.value.type);
  });
</script>
<style lang="less" scoped>
  @import url(./index.less);
</style>
