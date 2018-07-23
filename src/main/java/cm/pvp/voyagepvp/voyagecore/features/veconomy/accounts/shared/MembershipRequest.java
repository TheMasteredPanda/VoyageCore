package cm.pvp.voyagepvp.voyagecore.features.veconomy.accounts.shared;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Date;
import java.util.UUID;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class MembershipRequest
{
    private UUID requester;
    private Date date;
    private UUID accountId;

    public static Builder builder()
    {
        return new Builder();
    }

    public static class Builder
    {
        private UUID requester;
        private Date date;
        private UUID accountId;

        private Builder()
        {
        }

        public Builder requester(UUID requester)
        {
            this.requester = requester;
            return this;
        }

        public Builder date(Date date)
        {
            this.date = date;
            return this;
        }

        public Builder accountId(UUID id)
        {
            this.accountId = id;
            return this;
        }

        public MembershipRequest build()
        {
            return new MembershipRequest(requester, date, accountId);
        }
    }
}
