import { NumberInputEventHandler, OrderParam } from './OrderCommon';
import { Form, InputNumber } from 'antd';

export function OrderNumberInput({
    item,
    onChangeHandler,
}: {
    item: OrderParam;
    onChangeHandler: NumberInputEventHandler;
}): JSX.Element {
    return (
        <div className={'order-param-item-row'}>
            <div className={'order-param-item-left'} />
            <div className={'order-param-item-content'}>
                <Form.Item
                    name={item.name}
                    label={item.name+" :  "+item.description}
                    rules={[{ required: item.mandatory },  { type: 'number', min: 0 }]}
                >
                <InputNumber onChange={onChangeHandler} />
                </Form.Item>
            </div>
            <div className={'order-param-item-right'} />
        </div>
    );
}
