function after(hook, param)
	param:applyAndroidVersionFields()
	local ret = param:getResult()
	if ret == nil then
		return false
	end

    local fake = param:getSetting("android.build.version", "13")
    if fake == nil then
        return false
    end
    param:setResult(fake)
    return true, ret, fake
end
