import extensions from '../extensions/extensions';

// todo: move to useReducer which would enable extensions to access more easily data too
let idCounter = 0;
export function fetchJsonRpc(method, params, realDispatch) {
    let aborted = false;
    const dispatch = data => {
        if (!aborted) {
            realDispatch(data);
        }
    };
    const setError = error => dispatch({ type: `${method}-error`, value: error });
    const setData = data => dispatch({ type: `${method}-data`, value: data });
    const setLoading = value => dispatch({ type: `${method}-loading-state`, value });

    const id = idCounter++;
    setLoading(true);
    const controller = new AbortController();
    fetch(
        `/jsonrpc?m=${encodeURIComponent(method)}`,
        {
            method: 'POST',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json',
                ...(extensions.fetchJsonRpcHeaders || (function () {return {};}))(),
            },
            body: JSON.stringify(Array.isArray(params) /* assumed already wrapped */ ? params : {
                jsonrpc: '2.0',
                id,
                method,
                params,
            }),
            signal: controller.signal,
        })
        .then(res => {
            if (res.status !== 200) {
                setError({
                    message: `Invalid response status: HTTP ${res.status}.`,
                });
            } else {
                return res.json();
            }
        })
        .then(json => {
            if (!json) {
                return;
            }
            if (Array.isArray(json)) {
                if (json.some(it => it.error)) {
                    setError({
                        message: `Invalid response: ${json.filter(it => it.error).map(it => it.error).join('\n')}.`,
                    });
                } else {
                    setData(json.map(it => it.result));
                }
            } else if (json.error) {
                setError({
                    message: `Invalid response: ${json.error.message || ''}.`,
                });
            } else {
                setData(json.result);
            }
        })
        .finally(() => {
            setLoading(false);
            aborted = true;
        });
    return () => {
        aborted = true;
        if (!aborted) {
            controller.abort();
        }
    };
};