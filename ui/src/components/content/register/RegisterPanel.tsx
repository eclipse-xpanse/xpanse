/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

import { Alert, Button, Upload, UploadFile } from 'antd';
import { AppstoreAddOutlined, CloudUploadOutlined, HomeOutlined, UploadOutlined } from '@ant-design/icons';
import React, { useEffect, useState } from 'react';
import { RcFile } from 'antd/es/upload';
import { ObjectSerializer } from '../../../xpanse-api/generated/models/ObjectSerializer';
import { serviceVendorApi } from '../../../xpanse-api/xpanseRestApiClient';
import { Ocl } from '../../../xpanse-api/generated';
import DisplayOclData from './DisplayOclData';
import '../../../styles/register.css';

function RegisterPanel(): JSX.Element {
    type FileValidationStatus = 'notStarted' | 'inProgress' | 'completed';

    const [fileList, setFileList] = useState<UploadFile[]>([]);

    const [yamlSyntaxValidationResult, setYamlSyntaxValidationResult] = useState<JSX.Element | undefined>(undefined);
    const [isYamlSyntaxValid, setIsYamlSyntaxValid] = useState<boolean>(false);
    const [yamlSyntaxValidationStatus, setYamlSyntaxValidationStatus] = useState<FileValidationStatus>('notStarted');

    const [registerResult, setRegisterResult] = useState<String>('');
    const [isRegisterSuccessful, setIsRegisterSuccessful] = useState<boolean>(false);

    const [ocl, setOcl] = useState<Ocl | null>(null);
    const [isOclDataValid, setIsOclDataValid] = useState<boolean>(false);
    const [oclDataDisplay, setOclDataDisplay] = useState<JSX.Element | undefined>(undefined);
    const [registrationResultDisplay, setRegistrationResultDisplay] = useState<JSX.Element | undefined>(undefined);

    useEffect(() => {
        if (fileList.length > 0) {
            setYamlSyntaxValidationStatus('inProgress');
            const reader = new FileReader();
            if (fileList) {
                reader.readAsText(fileList[0] as RcFile);
                reader.onload = (e) => {
                    if (e.target) {
                        try {
                            setOcl(
                                ObjectSerializer.deserialize(
                                    ObjectSerializer.parse(e.target.result as string, 'application/yaml'),
                                    'Oclv2',
                                    ''
                                )
                            );
                            setYamlSyntaxValidationResult(
                                <Alert type={'info'} showIcon={true} message={'YAML Syntax Valid'} />
                            );
                            setIsYamlSyntaxValid(true);
                        } catch (e: any) {
                            console.log(e);
                            setYamlSyntaxValidationResult(<Alert type={'error'} showIcon={true} message={e.message} />);
                            setIsYamlSyntaxValid(false);
                        }
                    }
                };
            }
            setYamlSyntaxValidationStatus('completed');
        }
    }, [fileList]);

    useEffect(() => {
        if (registerResult) {
            if (isRegisterSuccessful) {
                setRegistrationResultDisplay(
                    <Alert
                        type={'success'}
                        message={`Service ${ocl?.name} Registered Successfully`}
                        closable={true}
                        onClose={onRemove}
                    />
                );
            } else {
                setRegistrationResultDisplay(
                    <Alert
                        type={'error'}
                        closable={true}
                        showIcon={true}
                        message={`Service Registration Failed`}
                        description={registerResult}
                        onClose={onRemove}
                        action={
                            <Button size='small' type='primary' onClick={onRemove} danger={true}>
                                Try Again
                            </Button>
                        }
                    />
                );
            }
        }
    }, [registerResult, ocl?.name, isRegisterSuccessful]);

    useEffect(() => {
        if (isYamlSyntaxValid) {
            if (isYamlSyntaxValid && ocl !== null && ocl !== undefined) {
                const oclTableData = DisplayOclData({ ocl: ocl });
                if (typeof oclTableData === 'string') {
                    setIsOclDataValid(false);
                    setOclDataDisplay(
                        <Alert
                            type={'error'}
                            showIcon={true}
                            message={`OCL data in the uploaded file not Valid. Error while parsing - ${oclTableData}`}
                        />
                    );
                } else {
                    setIsOclDataValid(true);
                    setOclDataDisplay(oclTableData);
                }
            }
        }
    }, [yamlSyntaxValidationResult, ocl, isYamlSyntaxValid]);

    const customRequest = () => {
        if (fileList) {
            serviceVendorApi
                .register(ocl as Ocl)
                .then(() => {
                    fileList[0].status = 'success';
                    setRegisterResult('Service Registered Successfully');
                    setIsRegisterSuccessful(true);
                })
                .catch((error: any) => {
                    setRegisterResult(error.message);
                    fileList[0].status = 'error';
                    setIsRegisterSuccessful(false);
                });
        }
        return;
    };

    const setFileData = (file: RcFile): boolean => {
        setFileList([file]);
        return false;
    };

    const onRemove = () => {
        setFileList([]);
        setOcl(null);

        setRegisterResult('');
        setIsRegisterSuccessful(false);

        setYamlSyntaxValidationStatus('notStarted');
        setIsYamlSyntaxValid(false);
        setYamlSyntaxValidationResult(undefined);

        setIsOclDataValid(false);
        setOclDataDisplay(undefined);
        setRegistrationResultDisplay(undefined);
    };

    return (
        <div className={'register-content'}>
            <div className={'content-title'}>
                <AppstoreAddOutlined />
                &ensp;Register Service
            </div>
            {registrationResultDisplay}
            <br />
            <div className={'register-buttons'}>
                <Upload
                    name={'OCL File'}
                    multiple={false}
                    beforeUpload={setFileData}
                    maxCount={1}
                    fileList={fileList}
                    onRemove={onRemove}
                    accept={'.yaml, .yml'}
                    showUploadList={true}
                >
                    <Button
                        size={'large'}
                        disabled={yamlSyntaxValidationStatus === 'completed'}
                        loading={yamlSyntaxValidationStatus === 'inProgress'}
                        type={'primary'}
                        icon={<UploadOutlined />}
                    >
                        Upload File
                    </Button>
                </Upload>
                <Button
                    size={'large'}
                    disabled={Boolean(registerResult) || !isOclDataValid}
                    type={'primary'}
                    icon={<CloudUploadOutlined />}
                    onClick={customRequest}
                >
                    Register
                </Button>
                {yamlSyntaxValidationResult}
            </div>
            <div>{oclDataDisplay}</div>
        </div>
    );
}

export default RegisterPanel;
