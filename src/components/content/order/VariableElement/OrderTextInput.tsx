import { Form, Input, Tooltip } from 'antd';
import { InfoCircleOutlined } from '@ant-design/icons';
import { OrderParam, TextInputEventHandler } from './OrderCommon';

export function OrderTextInput({
    item,
    onChangeHandler,
}: {
    item: OrderParam;
    onChangeHandler: TextInputEventHandler;
}): JSX.Element {
    return (
        <div className={'order-param-item-row'}>
            <div className={'order-param-item-left'} />
            <div className={'order-param-item-content'}>
                <Form.Item
                    name={item.name}
                    label={item.name+" :  "+item.description}
                    rules={[{ required: item.mandatory },  { type: 'string', min: 2 }]}
                >
                <Input
                    name={item.name}
                    placeholder={item.example}
                    suffix={
                        <Tooltip title={item.description}>
                            <InfoCircleOutlined style={{ color: 'rgba(0,0,0,.45)' }} />
                        </Tooltip>
                    }
                    onChange={onChangeHandler}
                />
                </Form.Item>
            </div>
            <div className={'order-param-item-right'} />
        </div>
    );
}