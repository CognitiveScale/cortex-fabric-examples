type rpcCallback = (obj: any) => void;

declare function registerRPC(funcName: string, func: rpcCallback): void;
declare function callRPC(funcName: string, obj?: any): void;
