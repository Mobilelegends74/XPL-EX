function after(hook, param)
	local ret = param:getResult()
	if ret == nil then
		return false
	end

	local setting = param:getSettingReMap("cell.phone.type", "phone.type", "2")
	if setting == nil then
	    return false
	end

	--PHONE_TYPE_CDMA=2
	--PHONE_TYPE_GSM=1
	--PHONE_TYPE_NONE=0
	--PHONE_TYPE_SIP=3

	local fake = tonumber(setting)
	if fake == nil then
	    fake = 2
	end

	if fake ~= 0 and fake ~= 1 and fake ~= 2 and fake ~= 3 then
	    fake = 2
	end

	param:setResult(fake)
	return true, tostring(ret), tostring(fake)
end