function after(hook, param)
	local res = param:getResult()
    if res == nil then
        return false
    end

	local country = param:getSetting("zone.country.name", "Iceland")
	local language = param:getSetting("zone.language.name", "Icelandic")
	if country == nil or language == nil then
	    return false
	end

	local fake = language .. " (" .. country .. ")"
	param:setResult(fake)
	return true, res, fake
end