import { ChangeEvent } from 'react';
import { CheckboxChangeEvent } from 'antd/lib/checkbox';

export type TextInputEventHandler = (event: ChangeEvent<HTMLInputElement>) => void;
export type NumberInputEventHandler = (value: number | string | null) => void;
export type CheckBoxOnChangeHandler = (e: CheckboxChangeEvent) => void;
export type SwitchOnChangeHandler = (checked: boolean) => void;
export type ParamOnChangeHandler =
    | TextInputEventHandler
    | NumberInputEventHandler
    | CheckBoxOnChangeHandler
    | SwitchOnChangeHandler;

export interface OrderParam {
    name: string;
    kind: string;
    type: string;
    example: string;
    description: string;
    value: string | number | boolean;
    mandatory: boolean;
    validator: string;
}

export interface OrderParamItemProps {
    item: OrderParam;
    [key: string]: any;
}