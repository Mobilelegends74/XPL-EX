function before(hook, param)
    local fake = param:randomInt(500, 99999);
    param:setResult(fake)
    return true, "Spoofed:", fake
end