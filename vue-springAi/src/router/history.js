import axios from '@/utils/axios-config.js';

export const getHistoryList = async (userId, pageNum = 1, pageSize = 10) => {
    const response = await axios.get('history/getHistory', {
        params: {
            userId,
            pageNum,
            pageSize
        }
    });
    return response.data;
};
